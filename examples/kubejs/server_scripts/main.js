// Passive Skill Tree 1.21.1: Path of Ascension.
// Replace the previous tree generator with this file. Restart after generation.
// JsonIO writes resource files; it does not reload the current resource snapshot.
(() => {
    const Registries = Java.loadClass('net.minecraft.core.registries.BuiltInRegistries')
    const ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const DIST = 100
    const skills = {}
    const magicAttributes = ['spell_power', 'mana_regen', 'max_mana']
    const hasMagic = magicAttributes.every(name =>
        Registries.ATTRIBUTE.containsKey(ResourceLocation.parse('irons_spellbooks:' + name)))

    function attribute(name, amount, operation) {
        if (!Registries.ATTRIBUTE.containsKey(ResourceLocation.parse(name))) {
            throw new Error('[SkillTree] Missing attribute: ' + name)
        }
        return { type: 'skilltree:attribute', attribute: name, amount: amount, operation: operation }
    }
    function percent(name, amount) { return attribute(name, amount, 1) }
    function flat(name, amount) { return attribute(name, amount, 0) }
    function xp(amount) {
        return { type: 'skilltree:gained_experience', multiplier: amount, experience_source: 'mobs' }
    }
    function refund(chance) { return { type: 'skilltree:free_enchantment', chance: chance } }
    function durability(chance) {
        return {
            type: 'skilltree:item_durability_loss_avoidance', chance: chance,
            item_condition: { type: 'skilltree:tag', tag_id: 'minecraft:enchantable/mining' }
        }
    }
    function magic(name, amount, fallback) {
        return hasMagic ? percent('irons_spellbooks:' + name, amount) : fallback
    }
    function node(branch, index, angle, radius, offset, title, icon, bonuses) {
        const id = branch + '_' + index
        const radians = angle * Math.PI / 180
        const root = index === 1
        const keystone = index === 6 || index === 7
        bonuses.forEach((bonus, i) => {
            // Stable ResourceLocation IDs replace the old UUID modifier format.
            if (bonus.type === 'skilltree:attribute') bonus.id = 'skilltree:ascension/' + id + '/' + i
        })
        skills[id] = {
            id: 'skilltree:' + id,
            title: title,
            titleColor: branch === 'mage' ? '55FFFF' : branch === 'warrior' ? 'FF5555' : 'FFAA00',
            positionX: Math.round(Math.cos(radians) * radius - Math.sin(radians) * offset),
            positionY: Math.round(Math.sin(radians) * radius + Math.cos(radians) * offset),
            buttonSize: root ? 32 : keystone ? 28 : 20,
            backgroundTexture: 'skilltree:textures/icons/background/' + (root ? 'class' : keystone ? 'keystone' : 'lesser') + '.png',
            iconTexture: 'minecraft:textures/item/' + icon + '.png',
            borderTexture: 'skilltree:textures/tooltip/lesser.png',
            isStartingPoint: root,
            isAlwaysStartingPoint: false,
            tags: root ? ['class'] : keystone ? [branch + '_keystone'] : [],
            bonuses: bonuses,
            requirements: [],
            directConnections: [], longConnections: [], oneWayConnections: []
            // Omit description: the mod generates accurate localized bonus tooltips.
        }
    }
    function link(branch, a, b) {
        skills[branch + '_' + a].directConnections.push('skilltree:' + branch + '_' + b)
        skills[branch + '_' + b].directConnections.push('skilltree:' + branch + '_' + a)
    }

    // Each class has two separate routes: 1 -> 2 -> 4 -> 6 and 1 -> 3 -> 5 -> 7.
    // Mage: spell damage versus mana sustain. Without Iron's: experience/enchanting.
    const mageRoot = hasMagic ? [
        percent('irons_spellbooks:spell_power', 0.10),
        percent('irons_spellbooks:mana_regen', 0.10),
        percent('irons_spellbooks:max_mana', 0.10),
        percent('minecraft:generic.max_health', -0.10),
        percent('minecraft:generic.attack_damage', -0.10),
        percent('minecraft:generic.armor', -0.10)
    ] : [xp(0.10), refund(0.10)]
    node('mage', 1, 330, DIST, 0, 'Path of the Mage', 'enchanted_book', mageRoot)
    node('mage', 2, 330, DIST + 70, -45, 'Arcane Studies', 'book', [magic('spell_power', 0.05, xp(0.05))])
    node('mage', 3, 330, DIST + 70, 45, 'Mana Reserve', 'lapis_lazuli', [magic('max_mana', 0.10, refund(0.05))])
    node('mage', 4, 330, DIST + 140, -65, 'Arcane Focus', 'blaze_powder', [magic('spell_power', 0.10, xp(0.10))])
    node('mage', 5, 330, DIST + 140, 65, 'Meditation', 'amethyst_shard', [magic('mana_regen', 0.10, refund(0.10))])
    node('mage', 6, 330, DIST + 215, -85, 'Archmage', 'end_crystal', [magic('spell_power', 0.20, xp(0.20))])
    node('mage', 7, 330, DIST + 215, 85, 'Arcane Wellspring', 'ender_pearl', [
        magic('max_mana', 0.20, refund(0.10)), magic('mana_regen', 0.20, xp(0.10))
    ])

    // Warrior: weapon mastery versus protection.
    node('warrior', 1, 210, DIST, 0, 'Path of the Warrior', 'iron_sword', [
        percent('minecraft:generic.attack_damage', 0.10), percent('minecraft:generic.max_health', 0.10),
        flat('minecraft:generic.armor', 1), percent('minecraft:generic.movement_speed', -0.05)
    ])
    node('warrior', 2, 210, DIST + 70, -45, 'Weapon Training', 'stone_sword', [percent('minecraft:generic.attack_damage', 0.05)])
    node('warrior', 3, 210, DIST + 70, 45, 'Endurance', 'apple', [percent('minecraft:generic.max_health', 0.05)])
    node('warrior', 4, 210, DIST + 140, -65, 'Battle Rhythm', 'iron_axe', [percent('minecraft:generic.attack_speed', 0.10)])
    node('warrior', 5, 210, DIST + 140, 65, 'Iron Guard', 'iron_chestplate', [flat('minecraft:generic.armor', 2)])
    node('warrior', 6, 210, DIST + 215, -85, 'Berserker', 'diamond_sword', [
        percent('minecraft:generic.attack_damage', 0.20), percent('minecraft:generic.max_health', -0.10)
    ])
    node('warrior', 7, 210, DIST + 215, 85, 'Bulwark', 'diamond_chestplate', [
        flat('minecraft:generic.armor', 3), flat('minecraft:generic.armor_toughness', 2),
        percent('minecraft:generic.movement_speed', -0.05)
    ])

    // Engineer: mining efficiency versus durable tools and building reach.
    node('engineer', 1, 90, DIST, 0, 'Path of the Engineer', 'redstone', [
        percent('minecraft:player.block_break_speed', 0.10), flat('minecraft:player.block_interaction_range', 0.5),
        durability(0.10), percent('minecraft:generic.attack_damage', -0.10)
    ])
    node('engineer', 2, 90, DIST + 70, -45, 'Efficient Mining', 'iron_pickaxe', [percent('minecraft:player.block_break_speed', 0.10)])
    node('engineer', 3, 90, DIST + 70, 45, 'Tool Maintenance', 'iron_ingot', [durability(0.10)])
    node('engineer', 4, 90, DIST + 140, -65, 'Precision Tools', 'diamond_pickaxe', [percent('minecraft:player.block_break_speed', 0.15)])
    node('engineer', 5, 90, DIST + 140, 65, 'Surveyor', 'compass_00', [flat('minecraft:player.block_interaction_range', 0.5)])
    node('engineer', 6, 90, DIST + 215, -85, 'Master Excavator', 'netherite_pickaxe', [percent('minecraft:player.block_break_speed', 0.25)])
    node('engineer', 7, 90, DIST + 215, 85, 'Master Artificer', 'clock_00', [
        durability(0.20), flat('minecraft:player.block_interaction_range', 1)
    ])

    ;['mage', 'warrior', 'engineer'].forEach(branch => {
        ;[[1, 2], [1, 3], [2, 4], [3, 5], [4, 6], [5, 7]].forEach(pair => link(branch, pair[0], pair[1]))
    })
    // Write only after the entire tree has been built and attributes validated.
    Object.keys(skills).forEach(id => JsonIO.write('kubejs/data/skilltree/skills/' + id + '.json', skills[id]))
    ;['alchemist', 'cook', 'hunter', 'tree'].forEach(id => {
        JsonIO.write('kubejs/data/skilltree/skill_trees/' + id + '.json', { id: 'skilltree:' + id, skillIds: [] })
    })
    JsonIO.write('kubejs/data/skilltree/skill_trees/main_tree.json', {
        id: 'skilltree:main_tree',
        skillIds: Object.keys(skills).map(id => skills[id].id),
        skillLimitations: { class: 1, mage_keystone: 1, warrior_keystone: 1, engineer_keystone: 1 }
    })
    console.info('[SkillTree] Generated ' + Object.keys(skills).length + ' skills: Mage, Warrior, Engineer. Magic mode: ' + hasMagic)
    if (!hasMagic) console.warn('[SkillTree] Iron\'s Spellbooks attributes unavailable; Mage uses experience and enchanting bonuses.')
})()
