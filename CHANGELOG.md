# 2.1.4
- Fixed in-compatability with Too Many Bows - rtxyd
- Fixed Tiered Socket desync - rtxyd
- Fixed bug with Homing Affix - rtxyd
- Fixed Catalyst socket rendering where it shouldn't - rtxyd
- Fixed Bow Affix issues with Apothic Curios - rtxyd

# 2.1.3
- Fixed Fabled Reforging Table crashes
- Fixed Sigil of Transmutation not working in some modpacks
- Fixed Performance issue with extra socket feature - rtxyd
- Fixed rare catalyst socket rendering bug - rtxyd

# 2.1.2
- Fixed crash caused by Trickster's armor set affix
- Fixed Fabled being more common than Ancient (Universal Bosses)
- Fixed Socket Bonus affix giving 0 sockets when item is Fabled rarity

# 2.1.1
- Fixed Tiered Sockets not working
- Fixed Server desync bug - rtxyd
- Added Rule description to Sigil of Ascension

# 2.1.0 - Huge Update
## Augments - rtxyd
- Completely Rewrote Augment System
- Added Malice and Convergence Augments
- Genesis Augment can now stack Apotheosis Invaders, 1 per rarity
- Gem Power Augment boost increased from 1.35 to 1.5
## Affixes
- Added Enchant Boost Affix, boosts a specific enchantment on the item
- Added Shield Guard Affix, reduces shield disable cooldown
- Added Entity Affixes for the Universal Boss feature: Apex, Arcane Shield, Colossus, Enrage, Executioner, Fear Aura, Hunter, Overload, Pack Leader, Phase, Predator, Regeneration, Shadow Step, Soul Drain, Thorns, Undying
## Affix Sets
- Added Trickster, Colossus and Deadeye Affix Sets
- Added Fabled Reforging for the Affix Set feature
## Gems and Sockets
- Backported Gem Case from 1.21.1 Apotheosis, compatible with any new rarity
- Added Tiered Sockets
- Added Catalyst Socket
- Added Hemophage Core (Blood) and Hallowed Relic (Holy) ISS gems
- Added Paradox Gem, grants Echoing Strike Bonus and revives the Double Strike Affix
- Renamed Augment Slots to Empty Inlay
## Universal Bosses
- Added bunch of new Attributes for them for more variety
- Universal Bosses now glow and show their rarity on hover
- Added Fabled Rarity for Universal Bosses
- Added Gear Bonus feature, boosts spawn chance based on your gear's rarity
- Fabled Universal Bosses now drop augments with a 5% chance (configurable), augment recipes removed
- Added Strict Mode config for Universal Boss Enchanting Books - rtxyd
## Rarity and Materials
- Added Fabled Rarity and Fabled Material
- Added Rarity Name Prefix to Affix Scrolls
## Crafting and Recipes
- Added Sigil of Confluence, combines affixed items - rtxyd
- Added Sigil of Elevation, elevates all tiered socket's rarity by one
- Added Sigil of Prismatic Conversion, unifies all sockets on the item into one, Catalyst Socket
- Added Salvaging Recipe for Affix Scrolls
- Improved the Erasure Recipe
## Compatibility
- Added Curios compatibility for Soulbound, curios are now preserved in their slot - rtxyd
## Misc
- Added Celestial Ranged category to Void Hunter
## Fixes
- Fixed fired arrows not triggering Slash again on a second impact - rtxyd
- Fixed Damage Affix for guns
- Fixed Spread Reduction Affix for guns
- Fixed Scroll Affix NBT not deleting on reforge
## Removed
- Removed Boss Spawner Conversion feature due to issues

# 2.0.9HF
- Fixed crash caused by Universal Boss Spawns

# 2.0.9
- Blocked the Reroll Button for Scroll Affixes visually
- Fixed category bug for Affix Scrolls, now they are appliable to all categories for that affix and also display all available categories
- Fixed the Universal Boss Json file not being overrideable with a datapack
- Added Additional Attributes condition for Spell Level Gems so they dont give an error in the log
- Added new Void Hunter Gem with the following bonuses:
- Void Hunter - On entity kill: teleport to the nearest entity, attack once, and gain the Echoing Strikes effect for a few seconds
- Arrow Teleport Slash - On Arrow Impact: Teleport to the entity/block (only if there is atleast one entity around) and deal damage to all entities in its trajectory in a radius - rtxyd
- Spell Echo - On Spell cast, cast the same spell again with different delay based on rarity
- Fixed I frame issue for Spell Cast Affix
- Nerfed the weight for Ragnarok, Yggdrasil and Abyssal Pearl Gems
- Added Dimension based rarities for Universal Boss feature and buffed the chance for the Empowered Mob spawns
- Added Concentration Affix for Staffs - Spells can't be interrupted by damage
- Fixed Fortify Affix, now works on entities and you lose the bonus armor correctly

# 2.0.8
- Improved Gem Power Augment logic and fixed Gem Flickering issue - rtxyd
- Muted Extra Gem Bonus registry and ported it to Fallen Lib instead - rtxyd
- Mana Block Affix - Block damage when above 200 mana, and lose mana based on damage amount +X% on every hit - rtxyd
- Fixed that blocks drop Affix Scrolls when an empowered creeper blows them up - rtxyd
- Fixed that every time you join the world already spawned mobs have a chance to become empowered again - rtxyd
- Added Gem Injected tag to fix potential bugs - rtxyd
- Now Scroll Affixes can't be rerolled in the Augmenting Table

# 2.0.7
- Fixed player-placed spawners converting into Boss Spawners
- Added a feature that lets the universal_boss.json file use tags, instead of just entity ids
- Affix Scroll Item now displays the category it goes on
- Added a Config to set default sockets for all items with a valid LootCategory, globally

# 2.0.6
- Removed SpellDamageEvent from AfflictedEventHandler, fixing the crash if ISS is not installed
- Fixed Gem Power not applying correctly
- Empowered enemies now have their rarity in their name

# 2.0.5HF
- Added ore compat to Prospector Affix, now highlights blocks tagged as #forge:ores and #c:ores in addition to vanilla ore tags

# 2.0.5
- Fixed a bug in apotheosis 1.20.1 which made it so multiple affixes targeting the same attribute don't apply correctly
- Removed the unnecessary "Makes the item Fabled" text from Supremacy Augment
- Removed the categories from Soulbound and Socket Bonus affixes, now they can appear on all categories
- Added Cooldown Reset Affix - X% chance to reset the cooldown of the casted spell
- Added Mana Shield Affix - While Above 50% mana, reduce incoming damage by X%
- Added Spell Focus Affix - Consecutive Casts of the same spell on the same target stack +X% damage up to X%, resets on spell switch or target switch
- Added Feast Affix - Killing an enemy restores X% of your max health instantly
- Added Berserker Affix - Each consecutive hit on the same target increases damage by X%, resets on target switch or taking damage
- Added Mana Leech Affix - On hit, restore X% of your missing mana.
- Added Spellblade Affix - Melee hits reduce your active spell cooldowns by X seconds. 
- Added Adaptive Affix - Each unique damage type you take gives X% resistance to that type for X seconds up to X% per type
- Added Fortify Affix - Your armor value scales up by X% when you are standing still
- Added Afflicted Affix - For each negative effect on you, gain X% damage and X% movement speed
- Added Prospector Affix - Nearby ores within X blocks glow through walls
- Implemented a new feature called Universal Bosses, which makes it so any hostile entity spawned can become an Apothic Boss, significantly making enemies stronger
- These type of enemies can drop 3 things: Affix Scrolls, Gem Dust and Enchant Books (Configurable in a loot modifier)
- Affix Scroll is a new item that carries a single random affix matching the tier of the enemy that dropped it.
- Apply a scroll to any compatible equipment (must have the same rarity) at an anvil for 15 Xp levels (configurable) to add that affix to the item
- Each item can hold up to 2 (configurable) scroll affixes. Use a Sigil of Erasure at the smithing table to remove all scroll-applied affixes from an item

# 2.0.4
- Moved client-only Minecraft reference in CascadeAugment to dedicated client class to fix the server incompatibility
- Added cap for Crit Chance & Damage on Cascade (100% for both) - rtxyd
- Made it so the socketed tooltip of Cascade only shows the scaled values when you are holding the item - rtxyd
- Fixed the HurtEventHandler weapon check - rtxyd
- Made it so you can't put items with Genesis Augment in them into the Augmenting Table - rtxyd
- Made Duality Augment work on staffs and changed the tooltip from X% Physical to X% Original - rtxyd

# 2.0.3
- Fixed Translation for Homing and True Shot Affixes
- Removed Double Strike because it was too overpowered and messy to implement correctly

# 2.0.2 — Huge Update
- Added 11 new affix types. 1 Weapon, 6 Bow and 4 Staff:
1. Autocast Affix - After casting a spell, autocast another spell instantly with no mana cost. 
2. Mana Cost Affix - Spells casted with this item cost % less mana 
3. Mana Return Affix - Spells casted with this item have % chance to return a % of their mana cost 
4. Mana Damage Affix - Spells casted with this item deal % more damage according to a % of your mana. 
5. Momentum Affix - An affix for bows that makes the arrow shot deal more damage the further it travels 
6. Piercing Arrow Affix - Arrows shot now pierce through enemies 
7. Multi Shot Affix - Every arrow you shoot shoots X more times 
8. Chain Shot Affix - On arrow kill, a new arrow shoots automatically towards the nearest enemy 
9. True Shot Affix - Arrows go through arrow immunity. 
10. Homing Affix - Arrows home onto the closest mob in its trajectory
11. Double Strike Affix - Your on-hits hit again for a % of the damage

- Added 3 New Augments. Augment of Genesis, Cascade and Duality:
1. Augment of Genesis: Sets the affixes power to 50%, on boss kill gain 0.05x to Gem Power and 0.05x Affix Power. Each boss is only counted once
2. Augment of Cascacde: On overcrits (when crit chance is above 100%), have 35% chance to deal 40% more damage. Both values scale with Crit Attributes
3. Augment of Duality: Crit Chance is set to 2x on the item, -30% Crit Damage and On Crits, deal 50% Magic and 50% Physical Damage

- Ice and Fire Compat. Added new gems based on Ice and Fire creatures
1. Abyssal Pearl: Weapons: +X% Damage while wet (in contact with water), Armor: +X% Damage Reduction while wet
2. Hydra Scale: Chestplates: Gain +X% Damage Reduction, but fire deals +X% more Damage to you, While on Fire the damage reduction is disabled, and Fire Resistance disables the Damage Reduction too. Can only be dropped from Hydras. Texture by @Pyrax
3. Ash of Yggdrasil: Weapons: Deal +X% Damage to Dragons, Armor: +X% Damage Reduction against dragons. Works the same as ragnarok.

- Other Changes:
1. Added new textures for Sigils and Augments. Credit to @Logar
2. Added Spawner to Boss Spawner Conversion - Every spawner in the world now has a chance to become a Boss Spawner. By default, it's 15%
3. Made it so if the categories field in Augments is empty, it displays all available categories. Genesis, Supremacy and Empowerment augments benefit from this.
4. Soulbound is no longer an Augment, it was reimplemented as an Affix again, but now it can only occur on Mythic and Ancient Gear

# 2.0.1HF
- Fixed JEI only showing the Soulbound augment (again)

# 2.0.1
- Fixed items being combinable in a smithing table if they both have a crest slot
- Fixed JEI only showing the Soulbound augment
- Added JEI tab for Transmutation recipe
- Added config for Max Number of Augment slots (default: 1)

# 2.0.0HF
- Fixed startup crash if Iron's Spellbooks is not installed

# 2.0.0 - Augments Update

## Augments - New Endgame System
- Augments are a new tier of power above gems. They're applied to **Crest Slots** (converted gem sockets).

**Available Augments:**
- **Soulbound** - Keep items on death
- **Gem Power** - Multiply gem effects by 1.35x
- **Supremacy** - Upgrades item to Fabled, increasing affix power by 1.5x. A star (⭐) indicates which affixes have been boosted

**How to Use:**
1. Craft augments in a crafting table using endgame materials
2. Convert gem sockets to Crest Slots using Sigil of Ascension in smithing table
3. Apply augments to Crest Slots via smithing table

**Configuration:**
- Augments are configurable via JSON at `data/fallen_gems_affixes/augments/`
- Customize which item types can use each augment
- Adjust power multipliers per augment

## New Items

**Crafting Materials:**
- Reinforced Gem Slate - Upgraded Gem-Fused Slate, core material for augment recipes

**Sigils:**
- Sigil of Ascension - Converts one gem socket to Crest Slot (smithing table)
- Sigil of Severance - Removes all augments from item, returns them to inventory (smithing table)
- Sigil of Transmutation - Transfers affixes, gems, sockets, and augments to another item of same category
  - Left: Sigil | Middle: Target Item | Right: Source Item

## New Affix
- **Socket Bonus** - Adds extra gem sockets to Mythic+ gear
  - Location: `data/fallen_gems_affixes/affixes/socket_bonus.json`
  - Available on all item categories

## Other Changes
- Added creative tab containing all mod items

## Requirements
- **New Dependency Required:** This version adds a new library dependency. Ensure you install the required library before launching.

# 1.3.2HF
- Fixed a crash caused by LivingEntityMixin

# 1.3.2
- Fixed the Spell Cast Affix being incompatible with the newest Iron's Spellbooks version

# 1.3.1
- Made the Celestisynth Attribute Patch optional, so it will no longer crash the game without Iron's Spellbooks
- Fixed Samurai and Warlord gems not being socketable into the Celestial Weapon category - Rtxyd
- Added new Gem Bonus called Boss Resistance for the Ragnarok gem, which reduces damage from bosses

# 1.3.0
- Fixed the Celestial Weapons category by separating Ranged and Melee Weapons
- Completely reimplemented the Max Health Damage attribute, so it's sort of balanced now and works as intended
- Deleted the Attributes made for Scguns and Switched the attribute affixes to the mod's attributes
- Fixed the Eldritch Spell Gem not showing up in chests
- Fixed log spam if Iron's Spellbooks is not installed - #7
- Removed the unnecessary CastResult check from SpellCastUtil - thanks Muon
- Add back the deleted fix for the crash caused by FakePlayer and non-vanilla sized inventory - Rtxyd

# 1.2.9
- Completely fixed the Soulbound Affix, items never get deleted, only equips item if it was in the equipment slot OR in offhand slot and only if it's equippable, also works with Corpse mod
- Added Spell Cast affix, that allows you to cast ISS spells on these triggers: spell_damage, spell_heal, hurt, melee_hit and projectile_hit
- Nerfed Hemolith Shard by changing the permanent Assassin effect on it

# 1.2.8
- Improved on the Soulbound affix by making it so armors stay in their slot, and improved on the code overall
- Fixed Permanent Effect config not working
- Added missing translations for Soulbound affix and changed the tooltip a little

# 1.2.7
- Added Soulbound Affix

# 1.2.6
- Fixed Fallen Warrior gem not showing up (sorry)

# 1.2.5
- Fixed Ragnarok Gem not showing up if celestisynth is not installed
- Fixed the "bleeding" mob effect for swords not showing up if celestisynth is not installed
- Added new Codec class (ConditonalLootCategory) for conditional loot categories, example can be seen in the Ragnarok gem file
- Improved the Boss Slayer bonus so it supports invaders (bosses) from Apotheosis
- Added BossUtils class for the change above
- Improved BossSlayerBonus code
- Added missing celestisynth:frostbound|irons_spellbooks:ice to the config

# 1.2.4HF
- Fixed Ragnarok gem having 3 rarities, while it should have all

# 1.2.4
- Added BossSlayerBonus and BossSlayerHandler classes
- Added Ragnarok gem, which has the bonus + damage against bosses, you can define bosses in a tag located at fallen_gems_affixes:tags/entity_types/boss_slayer.json
- Made the Cataclysm gems work when only cataclysm is installed, using the Extra Gem Bonus registry

# 1.2.3
- Fixed untranslated spell on Thunderheart Fragment gem
- Fixed a few Gem Bonuses not showing up
- Made ISS integration optional so no crashes occur

# 1.2.2
- Added AdaptiveSpellPowerAffix Class
- Added Spell Heal and Spell Damage targets, SPELL_DAMAGE_SELF, SPELL_DAMAGE_TARGET, SPELL_HEAL_SELF, SPELL_HEAL_TARGET
- Added bunch of new affixes for loot categories (Celestial Weapons, Staffs)
- Added Spell Power affixes to Celestial Weapons category
- Reworked all gems, this includes base apotheosis gems too
- Made Runiclib and Additional Attributes not a required dependency
- Added config for Celestisynth compat
- Improved Staffs category, so now all Staffs work, and not pulled by tag 
- Added compat to Alshanex's Familiars, T.O Tweaks and Cataclysm Spellbooks 
- The AdaptiveSpellPowerAffix and new targets have been backported from the amazing Apotheosis x Iron's Spellbooks mod, so makes sure to check that out if you are playing 1.21+ :D

# 1.2.1
- Added compat to Apotheotic Additions rarities, requires Esoteric Reforging too
- Added Forge conditions to all staff affixes

# 1.2.0
- Backported Extra Gem Bonus feature from 1.21.1 apotheosis
- Added Permanent Effect Bonus
- Added Attribute Effect Bonus
- Translated Staffs and Heavy Weapons category
- Cleaned up most code so there are no warnings in them

# 1.1.9
- Changed Howling Storm Gem texture, made by Chainsa
- Made Forge 47.4.0 the min version needed
- Nerfed Guardian of Hell Gem
- Added Multi Effect gem bonus type, fallen_gems_affixes:multi_effect, example can be found on the page
- Made RunicLib the new dependency

# 1.1.8
- Added config for SocketHelperMixin, so if you don't want gaps between sockets you can turn it off

# 1.1.7
- Rewrote SocketGemModifier class to use the logic from MobGearGemInjector
- Added SocketHelperMixin to allow sockets to have gaps between them, made by rtxyd 

# 1.1.6
- Fixed the issue where the gear you have would change the gems on it when leaving & reentering the world

# 1.1.5
- Added Gem of the Howling Storm for Scylla boss, reduced the drop chance for Cataclysm gems, epic: 50%, mythic: 25%, ancient: 5%
  Fixed the Innate Blood Gem, Fixed the gun category not working, fixed all the errors that showed up in the log

# 1.1.0
- Added two new classes, SocketGemModifier, which is a global loot modifier that adds gems to affixed loot in chests, and MobGearInjector, which is an event that adds gems to boss gear. Default chance is 30%, can be changed between 0-1, and can be turned off in fallen_gems_affixes.common.toml

# 1.0.1
- Changed version="1.20.1-1.7.0" to version="1.20.1-1.0.0" in mods.toml

# 1.0.0
- Release