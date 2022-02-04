## 1.05_00 BETA 2

SUMMARY:

- Complete rewrite of the patch.
- Reformatted console logs to make it easier to read.
- Created internal API to make new checks much faster & easier to implement.
- Every security check is now controllable via a file `security.yml`, located in `Zomboid/Server`. This allows for
  control of every check, turning them off, setting to `ignore` or `kick`, and variables for things such as `distance`
  checks.

FIXES:

- Removed safehouse and faction checks due to instability & de-syncs.

ADDED_FEATURES:

- Added Kotlin support.
- Added YAML support.
- Added ANSI support for console logs.
- Added `Zomboid/Server/security.yml` for servers to tweak all security checks as needed.
- Added `Zomboid/Logs/Craftboid/security.log`. This log is appended to everytime a player triggers an active security
  check. This also survives server restarts.
- Added checks for `SandboxOptions` packets.
- Added checks for `RemoveItemFromSquare` packets.
- Added checks for `StartFire` packets.
- Added checks for `SledgehammerDestroy` packets.
- Added checks for all database packets.
- Added pretty text when starting the server with Craftboid installed.

IMPROVEMENTS:

- Internal code hooks for security checks are now reduced to one-line calls.
- ChatFilter uses precompiled Regex patterns to make sorting and censuring texts faster.

## 1.04_01 & 1.04_02

FIXES

- Fixed security check for claiming safehouses.
- Removed safehouse check for days survived. (Seems like safe-houses in Build 41 needs reworking)

## 1.04_00

FIXES

- Re-deployed the original code for executing queries for the SQLite database.

ADDED_FEATURES:

- Faction invites sent to players from players who do not own the faction.
- Illegal phrases in usernames from `filters.txt`, rejecting logins.
- Players claiming safe-houses while not being near the safe-house.
- Players claiming safe-houses while not meeting the minimum days survived.
- Players sending sandbox options to the server.
- Players sending non-pvp zone data.
- Faction invites sent to offline or unknown players.
- Faction invites where the connection's ID does not match the ID sent in the packet.
- Faction invites accepted where the username of the player to accept isn't online.
- Faction invites accepted that weren't sent an invitation.
- Chat messages with authors being other than the player sending the packet.
- Sending damage to offline players.
- Sending damage to players greater than or equal to 64 units of distance.
- Players sending bandage packets to offline players.
- Players sending bandage packets to players greater than 20.0 units of distance apart.
- Players sending stitch packets to offline players.
- Players sending stitch packets to players greater than 20.0 units of distance apart.
- Players sending infection packets to offline players.
- Players sending infection packets to players greater than 20.0 units of distance apart.
- Players sending disinfect packets to offline players.
- Players sending disinfect packets to players greater than 20.0 units of distance apart.
- Players sending splint packets to offline players.
- Players sending splint packets to players greater than 20.0 units of distance apart.
- Players sending additional pain packets to offline players.
- Players sending additional pain packets to players greater than 20.0 units of distance apart.
- Players sending remove glass packets to offline players.
- Players sending remove glass packets to players greater than 20.0 units of distance apart.
- Players sending remove bullet packets to offline players.
- Players sending remove bullet packets to players greater than 20.0 units of distance apart.
- Players sending clean burn packets to offline players.
- Players sending clean burn packets to players greater than 20.0 units of distance apart.
- Illegal phrases in chat messages from `filters.txt`, censoring the message before broadcasting to other players.

IMPROVEMENTS

- Added `/Zomboid/Server/filters.txt` directory. Add a phrase on each line to filter out for usernames and chat.
    - Only filters with 3 characters or longer are added.
    - The command `/reloadoptions` will reload filters.
- Added check for players with full connections for player counts. (Should be more accurate)
- Fixed possible lockup of packets when sending to RakNet if the packet is either too big or fails to process prior to
  sending to client.
- Possible fix on handling sent inventory data due to improper dictionary lookup.
- Added `stop` alias for the `quit` command to stop the server.
- Improved logging code overall.
- Logs now go to staff chat for suspected hacks and ignored packets.
- Updated previous security checks to have a more informed statement when logging and kicking players for suspected
  hacking.

## 1.03_00

- Possibly fixed issue where players are randomly teleported outside safe houses when playing.

## 1.02_00

- Fixed issue where spawn houses releasing players inside the protection zone triggers the exploit check for
  teleporting.

## 1.01_00

- Initial public release.

## 1.00_00

- Initial private release.
