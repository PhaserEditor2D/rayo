/**
 * @param {Phaser.Game} game
 */
function $name$(game) {
	Phaser.Group.call(this, game);
}

$name$.prototype = Object.create(Phaser.Group.prototype);
$name$.prototype.constructor = $name$;