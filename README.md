MobLimiter
===========

Configuration
-------------

### Mob caps

The config.yml is made up of lines of `mob: limit`

**Mob** is the bukkit name for the entity. For example *cave spider* is displayed as *cave_spider* in the configuration

To limit a mobs numbers on restart/unload we define it like so:

    zombie: 4
    creeper: 10

The name of the **entity** followed by a valid **int** after a **:** is placed on a single line

Any mobs not given numbers in the config will be ignored and no limiting will happen to that specific mob type

*Please note, leaving emtpy lines in the config is a bad idea, don't do this, it won't crash or corrupt the config, it just makes things messy*

### Farm animal breeding caps

There are two additional options that can be specified in config.yml: `agecapbaby` and `agecapbreed`. These specify how long a farm animal should remain a baby and the adult breeding cooldown, respectively.

These are measured in ticks. A tick is at most 1/20th of a second, possibly longer depending on server load. The following example sets both values to 300 ticks (15+ seconds):

    agecapbaby: 300
    agecapbreed: 300

If the number of ticks is 0, baby farm animals will instantly become adults, and adults will have no breeding cooldown. If the number of ticks is less than 0, farm animal breeding is left to vanilla settings.

Other breedable entities (ocelots, wolves, villagers) are unaffected by these options.

Running
-------

The running of MobLimiter happens on **chunk unload** as well as **plugin disable** events.

This means when players navigate away from an area or log out with nobody nearby, the limits will be enforced to those chunks unloaded at that time.

It is also worth noting that limiting will happen on server shutdown **as well as reloads** due to the need for *onDisable* hooks.

Any issues or bugs feel free to create an issue via the [github issues](https://github.com/NerdNu/MobLimiter/issues) tracker.
