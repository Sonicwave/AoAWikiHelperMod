package net.tslat.aoawikihelpermod.weaponcategories;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.tslat.aoa3.item.weapon.archergun.BaseArchergun;
import net.tslat.aoa3.library.misc.AoAAttributes;
import net.tslat.aoa3.utils.ItemUtil;
import net.tslat.aoa3.utils.StringUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandPrintArchergunsOverview extends CommandBase {
	@Override
	public String getName() {
		return "printarchergunsoverview";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/printarchergunsoverview [clipboard] - Prints out all AoA archerguns data to file. Optionally copy contents to clipboard.";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
		World world = sender.getEntityWorld();

		if (!(sender instanceof EntityPlayer)) {
			sender.sendMessage(new TextComponentString("This command can only be done ingame for accuracy."));

			return;
		}

		if (!world.isRemote) {
			boolean copyToClipboard = args.length > 0 && args[0].equalsIgnoreCase("clipboard");

			if (copyToClipboard && server.isDedicatedServer()) {
				sender.sendMessage(new TextComponentString("Can't copy contents of file to clipboard on dedicated servers, skipping."));
				copyToClipboard = false;
			}

			List<String> data = new ArrayList<String>();
			List<BaseArchergun> archerguns = new ArrayList<BaseArchergun>();

			for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
				if (item instanceof BaseArchergun)
					archerguns.add((BaseArchergun)item);
			}

			archerguns = archerguns.stream().sorted(Comparator.comparing(archergun -> archergun.getItemStackDisplayName(new ItemStack(archergun)))).collect(Collectors.toList());

			data.add("{| cellpadding=\"5\" class=\"sortable\" width=\"100%\" cellspacing=\"0\" border=\"1\" style=\"text-align:center\"");
			data.add("|- style=\"background-color:#eee\"");
			data.add("! Name !! data-sort-type=number | Damage !! Unholster time !! Fire rate !! Durability !! Effects");
			data.add("|-");

			for (BaseArchergun archergun : archerguns) {
				ItemStack archergunStack = new ItemStack(archergun);
				String name = archergun.getItemStackDisplayName(archergunStack);
				String firingSpeed = (2000 / archergun.getFiringDelay()) / (double)100 + "/sec";
				String unholsterTime = StringUtil.roundToNthDecimalPlace(1 / (((int)(400 * (1 - (-ItemUtil.getStackAttributeValue(archergunStack, SharedMonsterAttributes.ATTACK_SPEED, (EntityPlayer)sender, EntityEquipmentSlot.MAINHAND, AoAAttributes.ATTACK_SPEED_MAINHAND)) / 100f))) / 100f), 2) + "s";

				data.add("| [[File:" + name + ".png|64px|link=]] '''[[" + name + "]]''' || {{hp|" + StringUtil.roundToNthDecimalPlace((float)archergun.getDamage(), 1) + "}} || " + unholsterTime + " || " + firingSpeed + " || " + archergun.getMaxDamage(archergunStack) + " || ");
				data.add("|-");
			}

			data.add("|}");
			CategoryTableWriter.writeData("Archerguns", data, sender, copyToClipboard);
		}
	}
}
