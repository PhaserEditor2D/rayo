/**
 * @param {Phaser.Game} game
 * @param {number} x
 * @param {number} y
 * @param {string} key
 */
function Sprite(game, x, y, key) {
	Phaser.Sprite.call(this, game, x, y, key);
}

Sprite.prototype = Object.create(Phaser.Sprite.prototype);
Sprite.prototype.constructor = Sprite;

Sprite.prototype.update = function () {
	// update code here
};