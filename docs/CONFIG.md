## `conditions`

The conditions used to determine how many players need to sleep to pass the night.

```yaml
# example
conditions:
  percentage: 0.7
  rounding-method: "ceil"
```

- `percentage` - Percent of players that must be sleep to pass the night
- `rounding-method` - Method to round the number of players required to sleep to the nearest whole number.

  **Options**:
    - `ceil` - Round up to the nearest whole number
    - `floor` - Round down to the nearest whole number
    - `round` - Round to the nearest whole number

## `exclusions`

```yaml
# example
exclusions:
  gamemode:
    enabled: false
    gamemodes:
      survival: false
      creative: true
      adventure: true
      spectator: true
  afk:
    enabled: false
    method: "afk-plus"
```

Rules for excluding players from sleeping.

Excluding means players will not be counted towards the amount of players needed to sleep.

This is helpful to exclude certain players such as those that are AFK.

If you were to have 3 players connected with one of them AFK and the sleeping percentage was set to 50%, only one of the
three players would need to sleep (50% of 2).

- `gamemode` - Exclude players from sleeping based on their gamemode

  **Options**:
    - `enabled` - Enable the gamemode exclusion
    - `gamemodes` - Gamemodes set to **true** will exclude players in that gamemode from sleeping
        - `survival` - Exclude players in survival mode from sleeping
        - `creative` - Exclude players in creative mode from sleeping
        - `adventure` - Exclude players in adventure mode from sleeping
        - `spectator` - Exclude players in spectator mode from sleeping
- `afk` - Exclude players from sleeping based on their AFK status

  **Options**:
    - `enabled` - Enable the AFK exclusion
    - `method` - Method to check for AFK status

      **Options**:
        - `afk-plus` - Use AFK status provided by [AFK+](https://github.com/LapisPlugins/AFKPlus) plugin

## `ignore-worlds`

```yaml
# example
ignore-worlds:
  - "world_nether"
  - "world_the_end"
```

- `ignore-worlds` - List of worlds to ignore. Vanilla sleeping mechanics will be used for these worlds.

## `messages`

You can use [MiniMessage](https://docs.advntr.dev/minimessage/format.html) ([cheatsheet](https://mmcs.sexnine.xyz)) to
format the messages.

The variables available vary based on the message type. You can use the `%` symbol to denote a variable.

You can remove or comment out (by putting a `#` in front) a message to suppress it.

```yaml
# example
messages:
  chat:
    player-entered-bed: "<light_purple>%player%<gray> is now sleeping (<light_purple>%num_sleeping%<gray>/<light_purple>%needed_sleeping%<gray>)"
    player-left-bed: "<light_purple>%player%<gray> is no longer sleeping (<light_purple>%num_sleeping%<gray>/<light_purple>%needed_sleeping%<gray>)"
    generic-sleeping: "<light_purple>%num_sleeping%/%needed_sleeping%<gray> players sleeping"
    # suppressed-message: "this is not a real message, just an example on commenting out a message to suppress it"
```

- `chat` - Messages sent to chat

  **Messages**:
    - `player-entered-bed` - Sent when a player enters the bed

      **Variables**:
        - `player` - The player that entered the bed
        - `num_sleeping` - The number of players currently sleeping
        - `needed_sleeping` - The total number of players required to pass the night
        - `more_needed` - The number of players more required to pass the night (will be 0 if more players sleeping than
          required)

    - `player-left-bed` - Sent when a player leaves the bed

      **Variables**:
        - `player` - The player that entered the bed
        - `num_sleeping` - The number of players currently sleeping
        - `needed_sleeping` - The total number of players required to pass the night
        - `more_needed` - The number of players more required to pass the night (will be 0 if more players sleeping than
          required)

    - `generic-sleeping` - Sent when the number of sleeping players changes

      **Variables**:
        - `num_sleeping` - The number of players currently sleeping
        - `needed_sleeping` - The number of players needed to sleep

## Miscellaneous options

### `disable-vanilla-action-bar`

```yaml
# example
disable-vanilla-action-bar: true
```

- `disable-vanilla-action-bar` - If set to `true`, players will not see the vanilla "X/Y players sleeping" and "Sleeping
  through the night" action bar messages. The [ProtocolLib](https://github.com/dmulloy2/ProtocolLib) plugin is required
  for this to work.

