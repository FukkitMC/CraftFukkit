# CraftFukkit
fabric craftbukkit implementation, now with 100% less recompilation errors

# Contributing
Fukkit is GPLv3 because craftbukkit is GPL

# Mixin
- all mixin methods must be prefixed with "fukkit_"
- if 1 injector replaces multiple lines of patches at once, it must be commented with "X birds with 1 stone", or something similar
- if there is a comment on the patch name the injector method a shortened version, for example `// CraftBukkit - added X` the injector should be named `fukkit_addedX`
- mixins must be postfixed with Mixin, (XMixin, ex. EntityMixin), and all duck interfaces and accessor mixins must be postfixed with Access, (ex. EntityAccess)
- all mixins and duck interfaces must belong in the relative package with their minecraft namespace accoriding to the yarn mappings
at the time they were written

# It doesn't run!
yea its not done yet, just port the patches verbatim for now until it's fixed

the gradle script is very hacky atm to solve a linkage error, but it works so don't touch it ;)

# Porting Patches
Port the patches to mixin as closely as possible, leaves less room for error, and ensures that behavior is identical, and it makes NMS support in the future easier.

## Constructor changes
These are the trickiest patches to deal with, if it's determined you can late init the object, create a static helper method in the Constructors class in the utils package, and use duck interfaces to lazily initialize the values. However, if it is required to set the values at the begining of the constructor or somewhere before the end, you must redirect *every single method and constructor* inside the constructor, and then do the logic afterwards in either the `Constructors` class or in a TAIL injector.

## Method Signature changes
Sometimes, CraftBukkit changes the signature of a method, to implement this, create a method in the duck interface for the class, and in the mixin, copy the *entire patched* method's logic into the implementing method. Then, optionally Overwrite the original method and throw an exception, leave it for me to clean up later, or inject into the old method with default values.

## Local Variables
These aren't that bad to deal with, if you suspect the method may be called concurrently, create ThreadLocal field, and make sure you reset the value at RETURN, or the last place you use it. **However**, if the method *may* be called recursively, for example many of the Block methods are subject to this, you should use a FIFO queue, there is a utility class called LocalVariable that you can use for this behavior (this is subject to memory leaks). You can also use a hashmap who's *entries* are weak and use a non-arg newly created object as a key (WeakHashMap wont work)

## Decompiler Errors
The best way to deal with them is to read the bytecode yourself

# Discord
---
https://discordapp.com/invite/KqE54TU

