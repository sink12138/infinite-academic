<template>
  <div>
    <Banner :title="{text: 'Institution', icon: 'mdi-home'}"></Banner>
    <div class="whole">
      <div class="message">
        <v-row>
          <v-col cols="3">
            <div class="roundIMG" style="float:left;">
              <img :src=logoUrl>
            </div>
          </v-col>
          <v-col>
            <div>
              <h1>
                {{name}}
              </h1>
            </div>
          </v-col>
        </v-row>
      </div>
      <BaseAnalytics :data0=data></BaseAnalytics>
    </div>
  </div>
</template>

<script>
import Banner from '../components/BaseBanner.vue'
import BaseAnalytics from "../components/BaseAnalytics.vue";

export default {
  components: {
    Banner,
    BaseAnalytics,
  },
  data() {
    return {
      data:[3,2,4,3,2],
      id:"GF_4ynwBF-Mu8unTG1hc",
      name:"机构名",
      interests:"科研方向",
      paperNum:0,
      patentNum:0,
      logoUrl:"https://ma-v3-images.azureedge.net/images/149899117.png"
    }
  },
}
</script>

<style>

</style>
