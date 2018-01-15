command ignore {
	[string:player] {
		perm utils.ignore;
		run ignore player;
		help Ignore a player.;
	}
	[empty] {
		perm utils.ignore;
		run list;
		help Lists everyone you ignore.;
	}
}
command unignore {
	[string:player] {
		perm utils.ignore;
		run unignore player;
		help Unignore a player.;
	}
}