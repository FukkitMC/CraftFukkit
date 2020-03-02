# Fukkit
fabric craftbukkit implementation, now with 100% less recompilation errors


# Contributing

Fukkit is GPLv3 because craftbukkit is GPL

# Mixin
- all mixin methods must be prefixed with "fukkit_", 
- all duck interfaces must be implemented with the @Implements annotation with the "fukkit$" prefix
- mixins must be postfixed with Mixin, (XMixin, ex. EntityMixin), and all duck interfaces and accessor mixins must be postfixed with Access, (ex. EntityAccess)
- all mixins and duck interfaces must belong in the relative package with their minecraft namespace accoriding to the yarn mappings
at the time they were written

# It doesn't run!
yea it doesn't the craftbukkit jar is not remapped correctly, just port the patches verbatim for now until it's fixed

the gradle is very hacky atm to solve a linkage error, but it works so don't touch it ;)
# Discord
---
https://discordapp.com/invite/KqE54TU

