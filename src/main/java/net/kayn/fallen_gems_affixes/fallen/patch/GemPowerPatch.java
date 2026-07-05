package net.kayn.fallen_gems_affixes.fallen.patch;

import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import net.kayn.fallen_gems_affixes.augment.GemBonusModifier;
import net.rtxyd.fallen.lib.api.IFallenPatch;
import net.rtxyd.fallen.lib.api.annotation.FallenPatch;
import net.rtxyd.fallen.lib.api.annotation.Targets;
import net.rtxyd.fallen.lib.service.DefaultPatchContext;
import net.rtxyd.fallen.lib.type.service.IFallenPatchContext;
import net.rtxyd.fallen.lib.type.service.IFallenPatchCtorContext;
import net.rtxyd.fallen.lib.type.service.IPatchDescriptor;
import net.rtxyd.fallen.lib.util.PatchUtil;
import net.rtxyd.fallen.lib.util.patch.InserterKey;
import net.rtxyd.fallen.lib.util.patch.InserterMethodData;
import net.rtxyd.fallen.lib.util.patch.InserterType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

@FallenPatch(
        priority = 1000,
        targets = @Targets(subclass = {GemBonus.class}),
        inserters = {GemBonusModifier.class})
public class GemPowerPatch implements IFallenPatch {
    private final IPatchDescriptor desc;
    public final List<String> patchedTargets = new ArrayList<>();
    // use independent logger to avoid class loading
    public final Logger logger = LogManager.getLogger("FallenGemsAffixes");

    public GemPowerPatch(IFallenPatchCtorContext iFallenPatchCtorContext) {
        desc = iFallenPatchCtorContext.currentPatch();
    }

    @Override
    public void apply(IFallenPatchContext iFallenPatchContext) {
        if (!(iFallenPatchContext instanceof DefaultPatchContext ct)) return;
        ClassNode cn = ct.getClassNode();
        logger.debug("Starting patch operation for {}. Current patch number {}", cn.name, patchedTargets.size());
        InserterMethodData hookMethod = iFallenPatchContext.getFallenInserter(
                InserterKey.of("net.kayn.fallen_gems_affixes.augment.GemBonusModifier",
                        "modifier",
                        InserterType.STANDARD));
        if (hookMethod == null) return;
        for (MethodNode method : cn.methods) {
            InsnList insns = method.instructions;
            // we must ensure the record class is clean, or it could be risky when we create instance.
            if (insns == null) continue;
            if (PatchUtil.isRecord(cn)
                    && PatchUtil.isCtor(method)
                    && !PatchUtil.isCleanRecordCtor(method, cn)) {
                logger.debug("Record is not clean, please check the risky class {}", cn.name);
            }
            // only patches like Map.get(key), or Map.getOrDefault(key),
            // and there are other cases, but will be strictly filtered.
            PatchUtil.insertMethodHook(cn,
                    method,
                    m -> m.getOpcode() == Opcodes.INVOKEINTERFACE
                            && m instanceof MethodInsnNode mn
                            && mn.owner.endsWith("Map")
                            && mn.name.startsWith("get")
                            && mn.desc.startsWith("(Ljava/lang/Object;")
                            && Type.getReturnType(mn.desc).getSort() != Type.VOID,
                    hookMethod);
        }
        patchedTargets.add(cn.name);
    }
}
