Grade: 100/100
Label your textboxes!!!!!!

In this file you should include:

Group members:
Ruoyao Yang(wustl id:474348), Haoyu Wu(wustl id:476438)


A description of the creative portion of the assignment

* Describe your features
  In this assignment, we design two parts of creative portion and a short delay to show the result of each game.
1. Game history board
In addition to wins and loses, users can track all historical games and they can see some information in the HistoryBoard Activity:
  How many chips they bet in this game
  Total points on user's cards
  Total points on computer's cards
  Who is the winner of this game

2. Lucky Bonus Rules!
Each Ace card will have an animation. Scale up and then scale down.
Within some rules, if the first Ace card is shown on the table, players will gain a luck bonus chance to double the bet.
To maintain the uncertainty of the game, we makes some rules for the bonus chance.
Rules:
(1)Before player places the bet, if there is an Ace card shown, player gets that chance.
(2)After player places the bet, if the first card player get is an Ace card, player gets that chance.
The first rule is to help player gain more rewards if they don't have so many bet to place.
The second rule is also a bonus but still keep the uncertainty of the game.(players also need to take a high risk this round)
When player gets the chance, they must have to choose before they do any operation. Swipe up to double and swipe down to not do.
 

* Why did you choose this feature?
1.Game history board
Users may want more information rather than just how many times they wins. So they need a history board to review previous games.
By checking the total numbers of each game and the bet they did, they also can analysis those data and make an adjustment.

2.Lucky Bonus Rules!
Blackjake appeals because of its uncertainty and surprises. So we want to introduce more rules to surprise our players and keep or even
increase the uncertainty of the game within a reasonable range.


* How did you implement it?
1.Game history board
(1)After each game ends, information of this game will be updated to Firebase Cloud DataBase
(2)Build a new HistoryBoard Activity, using LastAdapter to show data.
(3)After transfer to HistoryBoard, query data from Firebase Cloud DataBase by user's email

2.Lucky Bonus Rules!
(1)Set several Flag variables to record the states of games.
(2)Set several variables to record the first Ace card and whether player has made a choice.
(3)According to variables mentioned below, judge whether player can gain the bonus chance or whether player has made a choice.
(4)When player gain a bonus chance, there will be a dialog to inform player.
(5)Three new animations:
Scale animation: When each Ace appears, Ace cards will first scale up and then scale down.
Swipe up: When players gain a chance, they can swipe up to double the bet.
Swipe down: When players gain a chance, they can swipe down to give up the chance.
