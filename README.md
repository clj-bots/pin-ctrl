# pin-ctrl

Clojure library providing an abstraction layer for dealing with hardware and robotics pins (both analog and digital).

## Structure

There are two fundamental concepts to the library:

* `pin` - for dealing with pin state
* `board` - for dealing with board state, and pin mappings and information at the board level.

## Implementations

By itself, pin-ctrl does nothing.
Because it is only an API abstraction layer, an implementation of the protocols herein is required for any functionality.
These implementations are generally board specific, and so there will be separate libraries such as `clj-bots/pin-ctrl-firmata` and `clj-bots/pin-ctrl-rpi`, etc for each of the devices this may run on.

However, while eventually these libraries will be separate, during active development, we'll be including them as separate namespaces here in this project.
Once the API settles, we'll split them off.

## Status

As mentioned above, this project is still in early alpha, and is likely to change and be buggy.
However, the core API seems to be at least vaguely working with the simulation implementation.
You can test this out for yourself by investigating and running the `clj-bots.hello-world.sim` namespace.
This has some example usage of the simulator to see how things roughly work.

Other implementations will be coming fully online soon.

## Usage

For some usage sketches, please see the above mentioned `clj-bots.hello-world.sim` and other namespaces within `clj-bots.hello-world`.
Additionally, take a look at the [Marginalia documentation](https://clj-bots.github.io/pin-ctrl) for a more complete overview of the public API.
This documentation will be getting more fully fleshed out as the library matures.


## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

