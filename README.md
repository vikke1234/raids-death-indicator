# Raids death indicator
This plugin calculates the internal fraction and uses that to find out what you hit. Marks the NPCs as dead
if it would be, can be inaccurate when it is not calibrated. Once calibrated it should be accurate.

**Calibration happens automatically.**

*TLDR*:
- Red square in top right means that it's not calibrated
- Green square means that it's calibrated
- Pink square, something went wrong, please let me know where it happened

See [Docs](./docs.md) for how it works.

## Known issues
- HP Scaling is wrong (by a lot) in CoX
  - Solution: Change the checkbox in settings to whether the raid is a CM or not.
    - Will eventually be fixed


TODO 
--
COX: vangs 

Broken: kephri swarms, zebak jugs, kephri eggs (?)
verify: spitter, arcane, soldier

Fix prediction tree based on hitsplat, need to test Hitsplat::isMine

Add raid started event to tell others if it's a CM or not.
