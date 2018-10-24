# akka-casino-game

Playground for akka actors

## Game Description
We will try to implement a very simplified version of a slot machine with simple bonus game (doubling: red or black), ie: https://www.youtube.com/watch?v=yCodNJkfGOI

Our backend player will be modeled as an actor which can process different messages (requests) beying in different states:
INIT_STATE:
 - Start Game Request
 
ROLL_STATE:
 - Roll Request
 
TAKE_WIN_OR_GO_TO_BONUS_STATE:
 - Take win request
 - Go to double (bonus game) request

BONUS_STATE:
 - Red or Black card request
 
## Game Flow
![Gamne Flow](https://raw.githubusercontent.com/LvivScalaClub/akka-actor-game/master/docs/akka_game_flow.png "Game Flow")