function $name$() {
	Phaser.State.call(this);
}

$name$.prototype = Object.create(Phaser.State.prototype);
$name$.prototype.constructor = $name$;

$name$.prototype.preload = function () {
	// preload code here
	// this.load.pack("assets/pack.json", "$name$");
};

$name$.prototype.create = function () {
	// create code here
	// this.add.sprite(10, 10, "ball");
};