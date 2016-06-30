package mods.belgabor.mcpatcher;

import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;

import java.util.Arrays;

public class MCPatcherASMMod extends DummyModContainer
{
    public MCPatcherASMMod() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = MCPatcherConstants.MOD_ID + "asm";
        meta.name = MCPatcherConstants.MOD_NAME + " ASM";
        meta.description = "Fixes the Shelving Unit item deletion bug";
        meta.version = MCPatcherConstants.MOD_VERSION;
        meta.authorList = Arrays.asList("Belgabor");
        meta.credits = "cout970 & crew for MagnetiCraft, VikeStep for his ASM transformer tutorial, squeek502 for ASMHelper";
        
    }
    
    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        bus.register(this);
        return true;
    }
}
