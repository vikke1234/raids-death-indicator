(This is mainly for me to try to grasp the problem by typing it out)

# Intro

So what is the ping problem in the raids death indicator? It's boils down
to that if player A attacks and receives an xp drop. This xp drop is converted
damage which is then sent to player B. If player B is far away (e.g. A is from EU, B is from US),
the hitsplat that came from player A's attack can land before B receives the queued damage event.

This causes a problem, as currently when the hitsplat arrives, it subtracts the damage from
the queued damage. But if it does this before the queued damage has arrived, it will never
subtract the damage from the queued damage as it's not there.

This can make the queued damage scale infinitely, at some point there can be 100+ damage queued
which will mark enemies as dead much too soon.

# Proposed solutions
## Synchronized clocks

The first thing that comes to mind is to have synchronized clocks between party members.
Player A would send the expected damage along with a timestamp (current tick) to player B.

Player B would look at the animations of players, calculate when an attack *should* arrive.
If the event from player B is late, and should've already been processed it will not queue damage.

### Notes
After brief testing on syncing clocks, it seems to work pretty well.


### Problems
This approach has a number of problems:
1) Would need to make a list of attack animations, which needs to be updated whenever there' new weapons.
   Additionally, there' a million different weapons these days in use so it's annoying to add all of them
   in the first place.

2) BP -> chin does not have an animation for the chin, this will be incredibly annoying to work around,
   caps solves this by looking for the chin projectile but god damn...

3) How do we handle logouts and re-sync, logging out is apparently meta in CMs

4) If a person isn't in range to see animations, what should be the behavior in that case?


### Solutions to said problems
1) List of weapons and their delay formulas
2) Check for projectile in the future, initially just don't support
3) Upon relogging, make others send the current timestamp. 600ms is a "large" window but IF it's 
   late, use animations to re-sync the timestamp. We know locally when for example a person shadows,
   the queued damage event sends a timestamp when the projectile should land. From that we can tell
   when the attack happened on their side.
   It should be noted though that the others can for 1 hit be off slightly but I think the single event
   should just be ignored as it gets re-synced after that.
4) Ignore?