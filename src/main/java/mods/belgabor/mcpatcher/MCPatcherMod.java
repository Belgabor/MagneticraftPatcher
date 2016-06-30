package mods.belgabor.mcpatcher;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import squeek.asmhelper.com.belgabor.mcpatcher.ObfHelper;

import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"mods.belgabor.mcpatcher"})
@Mod(modid = MCPatcherConstants.MOD_ID, version = MCPatcherConstants.MOD_VERSION, name = MCPatcherConstants.MOD_NAME, dependencies = "required-after:Magneticraft@[0.6.0,)")
public class MCPatcherMod implements IFMLLoadingPlugin
{

    @Mod.EventHandler
    public void preInit (FMLPreInitializationEvent event) {
        MCPatcherRegistry.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MCPatcherRegistry.init();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{MCPatcherTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        ObfHelper.setObfuscated((Boolean) data.get("runtimeDeobfuscationEnabled"));
        ObfHelper.setRunsAfterDeobfRemapper(true);
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
