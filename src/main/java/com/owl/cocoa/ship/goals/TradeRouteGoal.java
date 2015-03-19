package com.owl.cocoa.ship.goals;

import com.owl.cocoa.common.SpacePosition;
import com.owl.cocoa.ship.TradeableItem;

public class TradeRouteGoal implements ShipGoal {

    public final TradeableItem potentialRoute;
    public final SpacePosition buyPosition;
    public final SpacePosition sellPosition;
    public final TradeRouteGoalStage stage;

    private TradeRouteGoal(TradeableItem potentialRoute, SpacePosition buyPosition, SpacePosition sellPosition, TradeRouteGoalStage stage) {
        this.potentialRoute = potentialRoute;
        this.buyPosition = buyPosition;
        this.sellPosition = sellPosition;
        this.stage = stage;
    }

    public TradeRouteGoal(TradeableItem potentialRoute, SpacePosition buyPosition, SpacePosition sellPosition) {
        this.potentialRoute = potentialRoute;
        this.buyPosition = buyPosition;
        this.sellPosition = sellPosition;
        this.stage = TradeRouteGoalStage.MOVE_TO_BUY;
    }

    public TradeRouteGoal withStage(TradeRouteGoalStage stage) {
        return new TradeRouteGoal(potentialRoute, buyPosition, sellPosition, stage);
    }
}
