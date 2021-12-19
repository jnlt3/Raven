# Raven
A competitive 1v1 specialist for robocode

Raven can be seen in [literumble 1v1 rankings](http://literumble.appspot.com/Rankings?game=roborumble). (Currently 7th best in the world)

<pre>
Licenced under the  Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License

Licence summary:
Under this licence you are free to:
     Share : copy and redistribute the material in any medium or format
     Adapt : remix, transform, and build upon the material
     The licensor cannot revoke these freedoms as long as you follow the license terms.

Under the following terms:
     Attribution:
           You must give appropriate credit, provide a link to the license, and indicate
           if changes were made. You may do so in any reasonable manner, but not in any
           way that suggests the licensor endorses you or your use.
     NonCommercial:
           You may not use the material for commercial purposes.
     ShareAlike:
           If you remix, transform, or build upon the material, you must distribute your
           contributions under the same license as the original.
     No additional restrictions:
           You may not apply legal terms or technological measures that legally restrict
           others from doing anything the license permits.

See full licencing details here: http://creativecommons.org/licenses/by-nc-sa/3.0/
</pre>


- Raven Targeting
     - Main Gun
     - Anti-Surfer Gun
- Raven Movement
     - Go-To Surfing
     - Hit Learning
     - Flattener
 
 
### Main Gun
The Main Gun is a KNN algorithm that is genetically tuned against the general population of the competition.

### AS Gun
The Anti Surfer Gun is another form of targeting that has been tuned against Wave-Surfing opponents. This requires it to be more adaptive as Wave Surfing opponents are learning ones.

### Go-To Surfing
In order to find the safest path to pick, Raven generates multiple paths at once and takes the danger of the each path. While doing so, it also accounts for the bot's effective width, which is also affected by velocity.

### Hit Learning
When Raven is hit or if two bullets hit each other, Raven can reliably use the bullet's angle, velocity and its current location to find where it was originally fired. By doing so, Raven accumulates information on the opponents targeting patterns and predicts where the next bullets will go. This information is later used by Go-To surfing that tries to minimize the danger.

### Flattener
Some targeting systems can't be easily learned and are very adaptive. In order to make its movement essentially uniform, Raven employs a technique called Flattening. This is the idea of moving in a way such that the opponent's learning algorithm won't be able to notice a pattern. Raven does this by keeping track of where it has gone in each situation and by marking these positions as dangerous.
