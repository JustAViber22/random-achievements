package com.randomachievements.achievement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

public enum RewardType {
	GRASS_BLOCKS {
		@Override
		public void give(PlayerEntity player) {
			giveStack(player, new ItemStack(Items.GRASS_BLOCK, 8));
		}
	},
	BREAD {
		@Override
		public void give(PlayerEntity player) {
			giveStack(player, new ItemStack(Items.BREAD, 5));
		}
	},
	MORE_FISH {
		@Override
		public void give(PlayerEntity player) {
			World world = player.getEntityWorld();
			ItemStack fish = new ItemStack(Items.COD, 3 + world.getRandom().nextInt(5));
			giveStack(player, fish);
		}
	},
	RANDOM_COW_LOOT {
		@Override
		public void give(PlayerEntity player) {
			World world = player.getEntityWorld();
			int r = world.getRandom().nextInt(4);
			ItemStack stack = switch (r) {
				case 0 -> new ItemStack(Items.LEATHER, 2);
				case 1 -> new ItemStack(Items.BEEF, 3);
				case 2 -> new ItemStack(Items.BONE, 4);
				default -> new ItemStack(Items.SADDLE, 1);
			};
			giveStack(player, stack);
		}
	},
	AXE_AND_PICK {
		@Override
		public void give(PlayerEntity player) {
			giveStack(player, new ItemStack(Items.STONE_AXE, 1));
			giveStack(player, new ItemStack(Items.STONE_PICKAXE, 1));
		}
	},
	AWKWARD_POTATOES {
		@Override
		public void give(PlayerEntity player) {
			giveStack(player, new ItemStack(Items.POTATO, 6));
			giveStack(player, new ItemStack(Items.POTATO, 9));
		}
	},
	SUSPICIOUS_STEW {
		@Override
		public void give(PlayerEntity player) {
			giveStack(player, new ItemStack(Items.SUSPICIOUS_STEW, 1));
		}
	},
	PEACE_FEATHER {
		@Override
		public void give(PlayerEntity player) {
			giveStack(player, new ItemStack(Items.FEATHER, 1));
		}
	},
	BED_SUPPLIES {
		@Override
		public void give(PlayerEntity player) {
			giveStack(player, new ItemStack(Items.WHITE_WOOL, 3));
			giveStack(player, new ItemStack(Items.OAK_PLANKS, 3));
		}
	};

	public abstract void give(PlayerEntity player);

	protected static void giveStack(PlayerEntity player, ItemStack stack) {
		if (!player.getInventory().insertStack(stack)) {
			player.dropItem(stack, false);
		}
	}
}
