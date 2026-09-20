# Textonic

Textonic is a realtime messaging product with private chats, rooms, and a lightweight community feed, built from networking fundamentals: a Java server, PostgreSQL persistence, a structured WebSocket protocol, a responsive vanilla web client, and the preserved TCP terminal client.

The web interface uses a fresh rounded visual system with equal light and dark themes, cobalt actions, teal presence, calm surfaces, and a deliberately simple message bar with one field and one Send action.

## What works

- Account registration and login with PBKDF2-HMAC-SHA256 password hashing
- Hashed, expiring session tokens
- Full public/private profiles with friends, followers, following, and request review
- Self-hosted customizable 3D profile avatars across chats, Social, profiles, and notifications, with initials as the fallback
- Friendship-gated private chats and follower-aware Social visibility
- Per-post Public or Followers audiences for public accounts, with author-controlled audience changes
- Realtime private and persistent room messages over WebSocket
- Durable private and room history
- Permanent sender-only deletion of private and room messages, synchronized to connected participants
- A realtime global Social feed with text posts and threaded replies
- In-app Event Pulse reminders for interested users, with direct event navigation and dismissal
- Post Rooms that turn an author's Social post into a linked, focused room with a member limit, open or approval-based joining, and optional expiry
- Owner join-request review plus editable room capacity and joining policy
- Permanent author-only deletion of Social posts and replies
- Account-aware Social likes that stay synchronized between the feed and post-detail conversation layer
- Reposts and quote-posts with embedded original context, privacy checks, and author-owned deletion
- Clickable `@mentions`, unread mention notifications, and indexed `#hashtag` topic results
- Private Social bookmarks with an owner-only Saved profile section
- Sharing Social posts to an existing private chat or joined room, with an optional personal caption
- Shared-post cards that return to the exact Social post, plus safe author previews with a Chat action
- SENT → DELIVERED → READ acknowledgement persistence
- Online/offline presence, last seen, and typing indicators
- Unified search for people, rooms, and viewer-authorized Social posts, including `@username` lookup and exact-post opening
- Safe room previews with public room codes, owner, member count, description, and Join action
- A personal sidebar containing only started private chats and joined rooms
- Persistent room creation, join, leave, ownership transfer, and archive authorization
- Joined-room member rosters with owner-only member removal
- Room and membership restoration after server restart
- Responsive desktop, tablet, and mobile web layouts
- Existing TCP terminal client using the same accounts, rooms, and messages
- Environment-based ports and database configuration
- Friends-only registration codes and in-process abuse limits for private beta
- Docker/Caddy packaging for HTTPS and WSS on a single public domain
- Non-destructive startup migration for existing data

## Requirements

- Java 21 or newer
- PostgreSQL
- The included PostgreSQL JDBC driver at `lib/postgresql-42.7.13.jar`
- Node.js 22 or newer only when rebuilding the pinned avatar-editor assets

New contributors should begin with the [code map](docs/code-map.md). It explains
each runtime layer, the browser file order, the request path, database evolution,
and where to add a change without mixing responsibilities.

## Start locally

Create a database for a fresh installation:

```sh
createdb private_messaging
psql -d private_messaging -f database/schema.sql
```

Then compile and run the server:

```sh
./scripts/run.sh
```

The repository already contains the built avatar studio. After changing
`web/avatar-editor-entry.js` or its pinned packages, rebuild that self-hosted
bundle before starting Java:

```sh
npm install
npm run build:avatar
```

Open [http://localhost:8080](http://localhost:8080), create an account, then search for a person, room, or post when you want to find something new.

The server also runs the structured WebSocket endpoint on port `55001` and preserves the TCP terminal endpoint on port `55000`. To open the terminal client in another shell:

```sh
./scripts/terminal-client.sh
```

Authenticate there with `/login <username> <password>` or `/register <username> <password>`.

## Configuration

All runtime configuration comes from environment variables; secrets do not belong in the repository.

| Variable | Default | Purpose |
|---|---:|---|
| `PM_DATABASE_URL` | `jdbc:postgresql://localhost:5432/private_messaging` | JDBC connection URL |
| `PM_DATABASE_USER` | current system username | PostgreSQL user |
| `PM_DATABASE_PASSWORD` | empty | PostgreSQL password |
| `PM_WEB_PORT` | `8080` | HTTP/static web port |
| `PM_WEBSOCKET_PORT` | `55001` | WebSocket port |
| `PM_TCP_PORT` | `55000` | terminal TCP port |
| `PM_TCP_HOST` | `localhost` | terminal client target host; ignored by the server |
| `PM_WEB_ROOT` | `web` | static client directory |
| `PM_PUBLIC_WEBSOCKET_URL` | derived from page host | public `ws://` or `wss://` URL behind a proxy |
| `PM_ALLOWED_ORIGINS` | same host as the WebSocket request | comma-separated exact browser origins allowed to open WebSockets |
| `PM_REGISTRATION_CODE` | disabled locally | when set, new accounts must supply this private-beta invite code |
| `PM_MAX_WEBSOCKET_CONNECTIONS` | `500` | total live browser-connection ceiling |
| `PM_MAX_WEBSOCKET_CONNECTIONS_PER_IP` | `64` | browser-connection ceiling per directly connected address |
| `PM_WEBSOCKET_AUTH_TIMEOUT_SECONDS` | `15` | time allowed to authenticate a new socket |
| `PM_WEBSOCKET_IDLE_TIMEOUT_SECONDS` | `120` | unresponsive socket lifetime before cleanup |

At every startup, the Java server applies additive schema changes and removes only expired sessions. The integrated web migration is in `database/migrations/001_integrated_web_client.sql`; search-first discovery and stable room codes are added by `database/migrations/002_search_first_discovery.sql`; private-beta hardening is in `database/migrations/003_private_beta_hardening.sql`; Social posts, replies, and shared-post message references are added by `database/migrations/004_social_feed.sql`; account-aware likes are added by `database/migrations/005_social_likes.sql`; author-owned deletion support is added by `database/migrations/006_owned_content_deletion.sql`; Post Rooms and join requests are added by `database/migrations/007_post_rooms.sql`; profiles, relationships, and post audiences are added by `database/migrations/008_profiles_relationships_privacy.sql`; editable profile bios are added by `database/migrations/009_editable_profiles.sql`; temporary Pulse requests/chats are added by `database/migrations/010_social_pulses.sql`; Help, Event, and Question Pulse metadata is added by `database/migrations/011_pulse_types.sql`; private Event reminder scheduling is added by `database/migrations/012_event_reminders.sql`; sender/author-owned editing timestamps are added by `database/migrations/013_owned_content_editing.sql`; private saved posts are added by `database/migrations/014_saved_posts.sql`; repost/quote links are added by `database/migrations/015_reposts_and_quotes.sql`; durable mentions plus hashtag indexing are added by `database/migrations/016_social_mentions_hashtags.sql`; and editable self-hosted profile avatars plus PNG previews are added by `database/migrations/017_profile_avatars.sql`. Existing private conversations are preserved and backfilled as accepted friendships. Editing preserves the original creation time and adds an `Edited` label. Deleting an owned message, post, or reply permanently removes its database row; deleting a post also cascades to its replies, likes, bookmarks, mentions, hashtag links, and reposts, and archives its linked Post Room.

Existing passwordless TCP-era usernames are deliberately not claimable through public registration. Create a new account name for beta use or migrate an intended legacy owner through a controlled administrator process before launch.

To recover a verified legacy owner without changing their user ID or existing data, run `./scripts/recover-legacy-account.sh <username>` locally and enter a new password at the hidden prompts. The command only updates an account whose password fields are still empty; it cannot overwrite a registered account password.

## Private-beta deployment

The repository includes a production container, PostgreSQL, and a Caddy HTTPS/WSS proxy. On a server with Docker and a domain pointed at it:

```sh
cp .env.beta.example .env
# Replace every example value in .env, then:
docker compose --env-file .env -f compose.beta.yml up -d --build
```

Verify `https://your-domain/api/health`, then give friends the HTTPS address and the separate registration code. Keep `.env` private and back up the `textonic_database` volume. See [Deployment](docs/deployment.md) for the full checklist.

The container has a database-aware health check and restart policy. Use `./scripts/backup-beta.sh /explicit/backup/directory` for a verified compressed database backup, then copy the archive off the application server.

## Test

Run compiler warnings and the dependency-free core tests:

```sh
./scripts/test.sh
```

With the server running and an existing test account, verify HTTP login plus WebSocket bootstrap:

```sh
PM_SMOKE_USERNAME=tester PM_SMOKE_PASSWORD='your-test-password' node tests/smoke-web.mjs
```

Run the stateful two-user private-beta suite only against a disposable or local database:

```sh
PM_BETA_TEST_ALLOW_WRITES=1 node tests/beta-integration.mjs
```

If registration codes are enabled, also set `PM_BETA_INVITE_CODE`. The suite creates uniquely named test users, one Social post and reply, private and room shares, and an archived temporary room; it deliberately refuses to run without the write opt-in.

The implementation was also exercised with two isolated accounts for private-message delivery/read state, room persistence, reload history, server restart restoration, responsive layouts, and clean shutdown.

## Architecture and protocol

- [Architecture](docs/architecture.md)
- [Code map](docs/code-map.md)
- [WebSocket protocol](docs/protocol.md)
- [Deployment](docs/deployment.md)
- [Database schema](database/schema.sql)
- [Third-party notices](web/THIRD_PARTY_NOTICES.txt)

## Terminal commands

- `/msg <username> <message>` — send a durable private message
- `/users` — list connected terminal users
- `/rooms` — list persistent rooms
- `/create <roomName>` — create and join a room
- `/join <roomName>` — join a room persistently
- `/leave <roomName>` — permanently leave a room
- `/delete <roomName>` — archive a room you own
- `/<roomName> <message>` — send a durable room message
- `/quit` — end the current session without leaving rooms

## Honest boundaries

Textonic is not end-to-end encrypted: TLS protects deployed transport, while the server stores readable message content. Room messages currently keep one aggregate lifecycle state per message rather than a separate read receipt per member. Attachments, push notifications, password reset, moderation tooling, and native clients are outside this build.
# Textonic-openalpha-
