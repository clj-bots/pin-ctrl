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

However, while eventually these libraries will be separate, while this project is in active development, we'll be including them as separate namespaces here in this project.
Once the API settles, we'll split them off.

## Usage

FIXME

## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
