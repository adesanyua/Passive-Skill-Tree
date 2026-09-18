# Как создавать навыки в Path of Ascension

Этот гайд относится к приложенному `server_scripts/main.js` и текущему порту
Passive Skill Tree на Minecraft 1.21.1. Методы `tree.skill()` из сторонних
примеров здесь не используются: генератор записывает JSON через `JsonIO`.

## 1. Где редактировать

- `kubejs/server_scripts/main.js` — навыки, бонусы, координаты и связи.
- `kubejs/assets/skilltree/lang/ru_ru.json` и `en_us.json` — переводы.
- `kubejs/assets/skilltree/textures/` — собственные PNG-иконки.
- `kubejs/data/skilltree/skills/` — результат генерации. Скрипт перезаписывает свои файлы при запуске.

Сначала запусти мир, чтобы скрипт записал данные, затем перезапусти игру.
Для одних только текстур и переводов можно перезагрузить ресурсы через F3+T.
Ошибки скрипта ищи в `logs/kubejs/server.log`, ошибки загрузки навыков — в `logs/latest.log`.

## 2. Добавить свой навык

Вставь этот код **внутрь `(() => { ... })()`**, перед комментарием
`// Write only after the entire tree has been built and attributes validated.`:

```js
node('engineer', 8, 90, DIST + 290, -85,
    'Алмазный бур', 'diamond_pickaxe', [
        percent('minecraft:player.block_break_speed', 0.15),
        flat('minecraft:player.block_interaction_range', 0.5)
    ])
link('engineer', 6, 8)
```

Получится навык `skilltree:engineer_8`: +15% к базовой скорости добычи и
+0,5 блока к дальности взаимодействия. Он соединён с `engineer_6` и автоматически
попадёт в `main_tree.json`. Номер должен быть свободным: повторный ID заменит узел.

Аргументы `node`: **ветка, номер, угол, расстояние от центра, боковое смещение,
название, иконка, массив бонусов**. Угол измеряется в градусах: 0 — вправо,
90 — вниз, 180 — влево, 270 — вверх. `DIST` сейчас равен 100.

В этом генераторе номер 1 означает старт класса, 6 и 7 — финальные навыки,
остальные — обычные. Чтобы связать разные ветки, добавь ID каждого навыка
в `directConnections` другого; `link()` рассчитан на одну ветку.

## 3. Название, цвет и иконка

Название в `node(...)` — обычный текст. Для перевода по языку игры оставь его пустым:

```js
skills.engineer_8.title = ''
skills.engineer_8.titleColor = '55FFFF' // HEX без #
```

Добавь ключ в существующий `ru_ru.json`, сохранив остальные записи:

```json
"skill.skilltree.engineer_8.name": "Алмазный бур"
```

В `en_us.json` используй тот же ключ со значением `Diamond Drill`.
Файлы JSON сохраняй в UTF-8; между записями нужны запятые, после последней — нет.

Аргумент иконки `diamond_pickaxe` означает
`minecraft:textures/item/diamond_pickaxe.png`, а не ID предмета.
Для часов и компаса используй `clock_00` и `compass_00`.

Для своей иконки положи квадратный PNG, например 32×32, сюда:
`kubejs/assets/skilltree/textures/icons/custom/drill.png`.
После создания узла укажи полный ресурсный путь:

```js
skills.engineer_8.iconTexture = 'skilltree:textures/icons/custom/drill.png'
skills.engineer_8.buttonSize = 24
skills.engineer_8.backgroundTexture = 'skilltree:textures/icons/background/notable.png'
```

Доступные рамки: `lesser`, `notable`, `class`, `keystone`, `gateway`, `recipe`.
`borderTexture` — отдельная рамка подсказки; текущий вариант
`skilltree:textures/tooltip/lesser.png` можно оставить.

## 4. Бонусы и описания

```js
percent('minecraft:generic.attack_damage', 0.10) // +10% базового урона
percent('minecraft:generic.max_health', -0.10)  // -10% базового здоровья
flat('minecraft:generic.armor', 2)              // +2 брони
xp(0.10)                                      // +10% опыта с мобов
refund(0.10)                                  // 10% шанс бесплатного зачарования
durability(0.10)                              // 10% шанс сохранить прочность добывающего инструмента
```

Бонусы Iron's Spellbooks требуют наличия его атрибутов. Генератор проверяет
атрибуты перед записью. `node()` сам присваивает уникальные стабильные ID
модификаторам: UUID и `modifierId` из старых примеров не нужны.

По умолчанию **не задавай `description`**: мод создаёт подсказку из реальных
бонусов. Если вместо названия тега видишь технический ключ, добавь его перевод.
Например, уже добавленный ключ `item_tag.minecraft:enchantable/mining` означает
«инструменты для добычи». Сам тег и его ID менять для этого не требуется.

Для ручного описания после создания узла можно написать:

```js
skills.engineer_8.description = [
    { text: '+15% к базовой скорости добычи', color: 'aqua' },
    { text: '+0,5 блока к дальности взаимодействия', color: 'aqua' },
    { text: '' },
    { text: 'Точность важнее грубой силы.', color: 'gray', italic: true }
]
```

`description` **заменяет автоматическое описание бонусов**. Текст не даёт
эффектов: их задаёт массив `bonuses`. При изменении чисел обновляй и ручное
описание. Для возврата к автоматическому удали `description`; пустой массив
скроет описание бонусов. При сетевой передаче текущий мод превращает компоненты
ручного описания в текст, поэтому для него здесь использованы строки `text`.

## 5. Стартовые узлы и ограничения

```js
skills.engineer_8.isStartingPoint = true // можно изучать без соседнего навыка
skills.engineer_8.tags = ['class']       // участвует в ограничении выбора класса
```

В конце генератора `skillLimitations: { class: 1, ... }` разрешает изучить
один узел с тегом `class`. Аналогично можно ограничить свои специализации:
назначь им общий тег и добавь его в `skillLimitations` с нужным числом.
Чтобы новый узел был финальным навыком инженера:

```js
skills.engineer_8.tags = ['engineer_keystone']
skills.engineer_8.backgroundTexture = 'skilltree:textures/icons/background/keystone.png'
```

Не соединяй такой узел после другого финального навыка с тем же тегом:
лимит 1 не позволит изучить оба. Подключи его к обычному узлу, например через
`link('engineer', 4, 8)` вместо связи с 6.

Изменение дерева не сбрасывает уже изученные навыки. Проверяй новые бонусы
на тестовом персонаже или после штатного сброса навыков.
