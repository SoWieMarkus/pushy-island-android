# How to play

Pushy has to get home: walk him into the hut <img src="../app/src/main/assets/sprites/finish.png" width="24" alt="finish"> to finish a level. The hut can only be
entered **from the south** (walk up into it), and only once every task of the level is done.

## Controls

| Action        | Touch                                    | Keyboard           |
| ------------- | ---------------------------------------- | ------------------ |
| Walk          | Swipe on the board or use the arrow pad | Arrow keys or WASD |
| Restart level | _Restart_ button                          | R                  |

The arrow pad, the inventory and the move counter sit in a panel on the right side of the screen. You can move
it to the left in _Settings_. Sounds can be switched off there as well.

## Rules of thumb

- **Pushing** – Pushy pushes one thing at a time and only on the same height: a box on sand can't be
  pushed up onto grass, and nothing can be pushed while Pushy stands on grass next to sand.
- **Heights** – Pushy can jump down from grass to sand, but not climb back up.
- **Walking against things** – Some objects aren't pushed but react when Pushy walks against them
  (levers, chests, flowers, the crab mother, …). Some only react from one side – see [Tiles](./tiles.md).
- **Inventory** – Items like coins, keys, seeds or energy are picked up by walking onto them and show up in
  the panel. They are used automatically when Pushy walks against something that needs them. Items can
  even be picked up when they lie on water next to Pushy.
- **Tasks** – Most levels have tasks that must be done before the hut opens: statues on their plates,
  every lamp lit, the pirate paid, … The [tile list](./tiles.md) says for each object whether it is a
  task and what it needs.

## Campaign

All levels of the original game plus new ones with logic gates are in the campaign. Levels unlock one after
another; solved levels can be replayed at any time.

## Custom levels

_Custom levels_ unlocks after level 1. There you can

- build levels with the editor (pencil, fill, erase, undo/redo, automatic smoothing of corners),
- play and solve them – a level can only be shared after you solved it yourself,
- share them as a code (`PUSHY2:…`) and import codes from friends with _Import code_.

The [web level editor](../editor) reads and writes the same codes and level files, and it also imports codes
of the old app.
