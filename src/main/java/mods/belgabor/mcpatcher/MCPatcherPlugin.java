package mods.belgabor.mcpatcher;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import squeek.asmhelper.com.belgabor.mcpatcher.ObfHelper;

import java.util.Map;

/**
 * Created by Belgabor on 22.06.2016.
 */

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"mods.belgabor.mcpatcher"})
public class MCPatcherPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{MCPatcherTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return MCPatcherMod.class.getName();
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        ObfHelper.setObfuscated((Boolean) data.get("runtimeDeobfuscationEnabled"));
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
