package com.github.fukkitmc.fukkit.util.craftbukkit.net.minecraft.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.EntityCustomNameToComponentFix;

public class PlayerNameFix extends DataFix {
	public PlayerNameFix(Schema outputSchema) {
		super(outputSchema, false);
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.fixTypeEverywhereTyped("Player CustomName", this.getInputSchema().getType(TypeReferences.PLAYER), (typed) -> typed.update(DSL.remainderFinder(), EntityCustomNameToComponentFix::fixCustomName));
	}
}
