<template>
  <div>
    <v-card class="betop">
      <BaseGoBack class="ml-6" style="float:left"></BaseGoBack>
      <div class="ma-auto d-inline-flex">
        <v-icon 
          class="ml-5 text-h2"
          color="indigo darken-4"
        >mdi-newspaper-variant
        </v-icon>
        <div class="my-font my-5">
          Journal
        </div>
      </div>
    </v-card>
    <div class="whole">
      <div class="message">
        <v-row>
          <v-col cols="3">
            <div class="roundIMG" style="float:left;">
              <img :src=coverUrl>
            </div>
          </v-col>
          <v-col>
            <div>
              <h1>
                {{title}}
              </h1>
              <h3>主办单位:{{sponsor}}</h3>
              <h3>ISSN:{{issn}}</h3>
            </div>
          </v-col>
        </v-row>
      </div>
      <BaseAnalytics :data0=data></BaseAnalytics>
    </div>
  </div>
</template>

<script>
import BaseGoBack from '../components/BaseGoBack.vue'
import BaseAnalytics from "../components/BaseAnalytics.vue";
export default {
  components: {
    BaseGoBack,
    BaseAnalytics,
  },
  data() {
    return {
      data:[3,2,4,3,2],
      id:"GF_4ynwBF-Mu8unTG1hc",
      title:"标题",
      interests:"科研方向",
      sponsor:"**大学",
      issn:"1234",
      paperNum:0,
      patentNum:0,
      coverUrl:"https://ma-v3-images.azureedge.net/images/149899117.png"
    }
  },
}
</script>

<style>

</style>
