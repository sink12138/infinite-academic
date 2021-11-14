<template>
  <div>
    <BasePaper :paperdata="paperdata"></BasePaper>
    <BaseGoBack></BaseGoBack>
  </div>
</template>

<script>
import BaseGoBack from '../components/BaseGoBack.vue'
import BasePaper from '../components/BasePaper.vue'
export default {
  data(){
    return{
      paperdata:{
            title:"固定时间稳定航天器相对轨道悬停控制",
            authors:new Map([
                        [
                            ["name","孙延超"],
                            ["id","01"]
                                                ],
                        [
                            ["name","李东禹"],
                            ["id","02"]
                                                ]
                                                    ]),
            abstract:"针对航天器相对空间目标的定点悬停控制需求,基于非线性反馈固定时间稳定算法设计了一种闭环悬停控制律.阐述了固定时间稳定的原理,以C-W方程为相对运动模型设计了悬停控制律,可以通过改变相关控制参数来消除初始悬停误差和干扰的影响,实现对控制效果的调节.结果分析表明,提出的控制律对初始悬停误差收敛速率较快,能够保持长时间、高精度定点悬停,仿真结果验证了方法的有效性.",
            keywords:["悬停控制","固定时间稳定","相对轨道","闭环控制","相对轨道控制"],
            doi:"10.11918/j.issn.0367-6234.2016.04.004",
            issn:"111111",
            isbn:"222222",
            citationNum:11,
            year:2016,
            journal:{
                id:"00000000000000",
                name:"哈尔滨工业大学学报",
                volume:"05",
                issue:"09",
                startPage:275,
                endPage:312
            },
            references:["1111111111111111111111111111111111",
                        "2222222222222222222222222222222222",
                        "33333333333333333333333333333333333"
            ],
            url:["www.baidu.com","www.jingdong.com"],
            publisher:"高等教育出版社"
        }
    }
  },
  components: {
    BaseGoBack,BasePaper
  },
}
</script>