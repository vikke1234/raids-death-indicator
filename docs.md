Background
--
OSRS xp drops are fractional, while the UI only shows integers, there's a hidden fraction in the background.
Whenever this fraction wraps around, you gain a "bonus" xp, so for example when you gain 53.4 and your fraction was
at .6, you'd actually gain 54 xp.


Hit calculations
--
While the xp calculations are known, and technically can be walked backward, the issue arises when the extra xp is
added to the drop. This is sometimes fine, as not all xp drops overlap (especially on higher hits). On lower hits,
the overlap is quite large, many often just differing by a single xp. Exact formulas can be found here:
https://oldschool.runescape.wiki/w/Combat#Experience_gain

As OSRS currently does not provide a means to see what your fraction is, it needs to be narrowed down through multiple
hits. Nylo death indicators does not need to do this because ToB does not have fractional xp drops, instead they
happen to be whole numbers only, thus is simpler.


How is the fraction found
--
@Caps Lock13 came up with the idea of using a "decision tree". The tree starts out by having all fractions (0-9)
available, whenever you receive an xp drop the available ones get cut off. There are two branches in the tree, one is
for when you receive the extra xp, the other is for when you don't. Eventually the tree gets to a point where only a
single leaf is still alive and that will be the internal fractions value.

TODO: example