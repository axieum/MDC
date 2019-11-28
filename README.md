<p align="center"> <img src="logo.png"></a></p>

<p align="center">
  <a href="https://github.com/Axieum/MDC/releases"><img src="https://img.shields.io/github/v/release/Axieum/MDC?include_prereleases"></a>
  <a href="https://github.com/Axieum/MDC"><img src="https://img.shields.io/github/languages/code-size/Axieum/MDC" alt="Code Size"></a>
  <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/github/license/Axieum/MDC" alt="License"></a>
</p>

# MDC
> Unify the Minecraft ⟷ Discord chat

_Please see the [wiki](https://github.com/Axieum/MDC/wiki) for help on getting started!_

## Features
* Flexible configuration
* Multiple channel support
* Lots of message placeholders to personalise your Discord chat
* Filter by dimension
  * Have messages from specific dimensions go to specific channels
  * Have messages from specific channels go to specific dimensions
* Full markdown support
  * Enable or disable emoji translations (i.e. `:emoji_name:`)
    * May be useful if a custom font allows you to use unicode characters?
* Many Minecraft message events
  * `started`, `stopping`, `stopped`, `crashed`, `join`, `leave`, `death`, `advancement`, `chat`, `dimension`
* Some Discord message events
  * `chat`, `edit`, `react`, `unreact`, `attachment`
* Built-in and configurable TPS and uptime commands
* Proxy Minecraft commands through Discord
  * Configure Discord commands that can execute commands on the server
  * Customise the command prefix (i.e. `!command`)
  * Full control of Discord member permissions and channels per command
* Periodically update the presence of the Bot (i.e. "Playing ...")
  * This will rotate through presence entries in the config
* _An API for developers to register their own Discord commands*_

## Commands
Note, that it is required you add the following UUID as an operator in order to execute protected commands `3665cd17-b83f-43b3-848c-e4d305271340`.
```json
{
    "uuid": "3665cd17-b83f-43b3-848c-e4d305271340",
    "name": "@BotName#Tag",
    "level": 4
}
```

## Configuration
Please see [mdc-common.example.toml](mdc-common.example.toml) for an example configuration file.

There are many placeholders available, take a look at the comments in the example config.

### Date/Time Formatting
For example, `{{DATETIME|d/MM/yyyy '@' hh:mma}}`.

You can find all patterns over at the [Java Docs](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html#patterns).

### Duration Formatting
For example, `{{ELAPSED|H'hrs' m'mins' s'secs'}}`.

| Character | Description    |
|-----------|----------------|
| y         | years          |
| M         | months         |
| d         | days           |
| H         | hours          |
| m         | minutes        |
| s         | seconds        |
| S         | milliseconds   |
| 'text'    | arbitrary text |

## Acknowledgements

- [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA)
- [emoji-java](https://github.com/vdurmont/emoji-java)
- [java-diff-utils](https://github.com/java-diff-utils/java-diff-utils)

## License

MDC is open-sourced software licensed under the [MIT license](https://opensource.org/licenses/MIT).
