command ignore {
	[string:player] {
		perm utils.ignore;
		run ignore player;
		type player;
		help Ignores or Unignores a player.;
	}
	[empty] {
		perm utils.ignore;
		run list;
		type player;
		help Lists everyone you ignore.;
	}
}
command unignore {
	[string:player] {
		perm utils.ignore;
		run unignore player;
		type player;
		help Unignore a player.;
	}
}