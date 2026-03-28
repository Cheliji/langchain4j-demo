package com.cheliji.ai.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class CityInformation {

    @Tool("返回给定城市的天气预报")
    public String getWeather(@P("指定城市")String city) {
        return city + "最近几天下雨" ;
    }


    @Tool("返回给定城市现在的道路情况")
    public String getRoadInformation(@P("指定城市")String city) {
        return city + "路况不错,不塞车" ;
    }

}
