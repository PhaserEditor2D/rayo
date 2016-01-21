function main() {
	var game = new Phaser.Game(800, 600);
	// game.state.add("Boot", Boot);
	// game.state.add("Preload", Preload);
	// game.state.add("Level", Level);
	game.state.start("Boot");
}