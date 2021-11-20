<template>
  <div>
    <v-card class="betop">
      <BaseGoBack class="ml-6" style="float:left"></BaseGoBack>
      <div class="ma-auto d-inline-flex">
        <v-icon 
          class="ml-5 text-h2"
          color="indigo darken-4"
        >mdi-home
        </v-icon>
        <div class="my-font my-5">
          Institution
        </div>
      </div>
    </v-card>
    <div class="whole">
      <div class="message">
        <h2>
          {{name}}
        </h2>
        <h3>科研方向:{{interests}}</h3>
        <h3>文章数量:{{paperNum}}  专利数量:{{patentNum}}</h3>
      </div>
      <BaseAnalytics :data0=data></BaseAnalytics>
    </div>
  </div>
</template>

<script>
import BaseGoBack from '../components/BaseGoBack.vue'

export default {
  components: {
    BaseGoBack,
  },
  data() {
    return {
      data:[3,2,4,3,2],
      id:"GF_4ynwBF-Mu8unTG1hc",
      name:"机构名",
      interests:"科研方向",
      paperNum:0,
      patentNum:0,
    }
  },
}
</script>

<style>

</style>
