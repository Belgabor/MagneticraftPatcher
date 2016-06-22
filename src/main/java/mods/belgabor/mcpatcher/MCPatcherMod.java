package mods.belgabor.mcpatcher;

import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;

import java.util.Arrays;

public class MCPatcherMod extends DummyModContainer
{
    public MCPatcherMod() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "magneticraftpatcher";
        meta.name = "MagnetiCraft Patcher";
        meta.description = "Fixes the Shelving Unit item deletion bug";
        meta.version = "1.7.10-0.1";
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
