# MDC
> Unify the Minecraft <-> Discord chat

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