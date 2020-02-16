package xyz.xy718.getdrops.event;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.world.GenerateChunkEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.ChunkTicketManager.LoadingTicket;

import xyz.xy718.getdrops.GetDropsPlugin;
import xyz.xy718.getdrops.util.ItemUtil;

public class ChunkEvent {

    private Logger LOGGER = GetDropsPlugin.LOGGER;
    
    @Listener(order = Order.POST)
    public void chunkLoad(
    		LoadChunkEvent event
    		) {
    	Chunk targetChunk=event.getTargetChunk();
    	if(targetChunk.getEntities().size()>0){
        	LOGGER.debug("区块加载:"+targetChunk.getPosition());
        	LOGGER.debug("实体数量:"+targetChunk.getEntities().size());
    	}
    	//是否有实体
    	if(targetChunk.getEntities().size()<=0) {
    		return;
    	}
    	List<Entity> drops=targetChunk.getEntities()
	    	.stream()
	    	.filter(e -> e.getType().equals(EntityTypes.ITEM))//留下类型是掉落物的实体
	    	.filter(e -> ItemUtil.isTrackingItem(e.getUniqueId()))//留下正在追踪中的掉落物
    	.collect(Collectors.toList());

    	//检测区块内物品是否有拥有者
    	if(drops.isEmpty()) {
    		return;
    	}
    	for(Entity e:drops) {
        	//检测其拥有者在不在区块追踪列表中来判断   是否是由捡拾而造成的区块加载
    		UUID pUUid=ItemUtil.getOnTrackingItem().get(e.getUniqueId());
    		if(ItemUtil.getChunkTrackingPlayers().contains(pUUid)) {
    			ItemUtil.singlePickupAction(e, pUUid);
				/*
    			//调度器一定tick后捡拾物品
    			Task.builder()
				.execute(() ->{
	    			
				})
				.delayTicks(20)
				.name("GetDrops-taskPickupItem")
				.submit(GetDropsPlugin.get());
    			*/
		    	//捡拾后无论成功与否都卸载区块，因为该区块是由捡拾而加载的
				//设定被加载的区块200tick后卸载
				Task.builder()
					.execute(() ->{
						/*
				    	Optional<LoadingTicket> ticket= GetDropsPlugin.getTicketManager()
				    			.createTicket(GetDropsPlugin.get(), targetChunk.getWorld());
						ticket.get().unforceChunk(targetChunk.getPosition());
						*/
						targetChunk.unloadChunk();
					})
					.delayTicks(200)
					.name("GetDrops-unLoadChunk")
					.submit(GetDropsPlugin.get());
    		}
    	}
    	//
    	 
    }
    @Listener
    public void chunkUnLoad(
    		UnloadChunkEvent event
    		) {
    	LOGGER.debug("区块卸载====:"+event.getTargetChunk().getPosition());
    }
}