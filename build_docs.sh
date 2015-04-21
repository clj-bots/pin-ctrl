#!/usr/bin/env bash

#lein marg -c styles.css
lein marg -c styles.css -m src/clj_bots/pin_ctrl.clj src/clj_bots/pin_ctrl_simulator/implementation.clj src/clj_bots/pin_ctrl/implementation.clj src/clj_bots/pin_ctrl/protocols.clj src/clj_bots/pin_ctrl/state_wrapper.clj src/clj_bots/hello_world/sim.clj

