MobLimiter
===========

Version 2 of MobLimiter, featuring three configurable Limiter engines that control the mob population in different ways.

While the original version of MobLimiter was primarily designed to cull mobs on chunk unload, with limited support to
prevent new spawns in real time, MobLimiter 2 is designed from the ground up to be more proactive about managing the
spawning and removal of entities. The Limiters are:

* **Age:** Mobs can be configured to have a maximum lifespan (in ticks) that results in the entity being killed and
dropping its items after the limit is reached. ("Special" mobs are exempt.) MobLimiter will try to not kill breeding
pairs of farm animals if it can, by not killing farm animals if there would be less than two in the chunk.

* **Spawning:** MobLimiter can be configured to limit the spawning of new mobs in real time, checking the number of the
applicable mob type in a "view distance" (as a chunk radius) as well as in an individual chunk and blocking the addition
of extra mobs beyond the limit.

* **Chunk Unload:** The chunk-unload culling feature of MobLimiter 1.x is still available, if its use is desired.


Configuration
-------------

### General Settings

* `radius`: The "view distance" to check for mobs, as a chunk radius (e.g. 3 would be a 7x7 area)
* `breeding_ticks`: Farm animal breeding cooldown in ticks (-1 to disable)
* `growth_ticks`: Ticks for a farm animal to grow up (-1 to disable)
* `logblock`: Enable LogBlock support. More below.
* `debug`: Print debugging info to console


### Default Limits

The `defaults` block defines limits that will globally apply to any mob type that doesn't have an explicit override
defined in the `limits` block. (Undefined values fall back to `-1`, for disabled.) Specific mob limits *inherit* the
default block, with any defined fields overriding the value from `defaults`.

```
defaults:
  age: 18000 #15 minutes in ticks
  max: 200 #200 in "view distance"
  chunk_max: 50 #50 in a single chunk
  cull: 4 #cull mobs down to this maximum on chunk unload
```

* `age`: Enable age limiting and remove the mob after a number of ticks. (e.g. 18000 for 15 minutes)
* `max`: The maximum number of a mob type to be allowed to spawn in a "view distance" defined by `radius`.
* `chunk_max`: The maximum number of a mob type to be allowed to spawn in a single chunk.
* `cull`: If set to a value other than `-1`, the number of mobs to *not* be removed on chunk unload.


### Individual Mob Limits

The `limits` block allows you to specify limits that apply to individual mob types. These inherit the values defined in
`defaults`, overriding the values.

Mob types are named using their Bukkit EntityType string, with the exception of `dyed_sheep` targeting sheep that have 
a dye color applied.

```
limits:
  skeleton:
    max: 100
    chunkMax: 30
    age: 12000
  cow:
    chunkMax: 75
    age: 12000
  horse:
    age: -1
  villager:
    max: 200
    chunkMax: 50
    age: -1
```


### Farm Animal Breeding Tweaks

The `breeding_ticks` and `growth_ticks` fields define how many ticks a farm animal will remain a baby and the breeding 
cooldown, respectively. If your server is running at a full 20 ticks per second, a value of 400 for each would make
the respective values approximately 20 seconds.

If the value is set to zero, there will be no delay and the condition will be instantaneous. A value of -1 will disable
tampering with vanilla breeding behavior.

This function only affects farm animals, and ignores other breedable entities like ocelots, wolves and villagers.


### Special Mobs

MobLimiter will not remove any mobs that are deemed to be "special" in some way that may make their removal undesirable.

The criteria include:

* Mobs with custom names, such as from a name tag
 
* Tamed mobs

* Elder guardians. (Regular guardians can be limited, but Elder ones won't be touched.)

* Any mob that is holding an item, as it may have picked up a player's equipment.


### LogBlock Integration

If LogBlock is running on the server, you can enable LogBlock integration by setting the `logblock` field to true in the
 config file. When enabled, mob removals will be tracked as kills in LogBlock when MobLimiter performs a chunk unload 
 cull or age limit kill.

Age limit kills are logged with a weapon of `watch` and chunk unload culling uses `gold sword`, both using a "player" 
name of `MobLimiter`.


### Commands

* `/moblimiter` — Prints a description of what MobLimiter does. Available to all users.

* `/moblimiter reload` — Reload the plugin configuration. Requires `moblimiter.reload`.

* `/moblimiter count` — Count all living entities in your chunk and view radius. Requires `moblimiter.count`.

* `/moblimiter limits` — Print all configured limits. Requires `moblimiter.limits`.

* `/moblimiter check` — Inspect the mob you're looking at, printing its age, limits and statuses. Requires `moblimiter.check`.

All commands can be accessed with the `moblimiter.*` permission node.

