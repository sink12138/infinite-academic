<template>
  <v-card min-width="240">
    <v-list dense class="text-left">
      <v-subheader class="text-body-1 text-left black--text">
        <v-icon v-if="type == '学科'">
          mdi-flask-empty
        </v-icon>
        <v-icon v-else>
          mdi-tooltip-check
        </v-icon>
        <span>热门{{this.type}}</span>
      </v-subheader>
      <div class="text-subtitle-2 ml-3 grey--text text--darken-2">
        {{this.type}}根据热度排序
        <v-tooltip right>
          <template v-slot:activator="{ on, attrs }">
            <v-icon 
              dense
              v-bind="attrs"
              v-on="on"
            >
              mdi-information
            </v-icon>
          </template>
          <div class="text-left">热度根据实体的被引量<br/>进行加权计算得出</div>
        </v-tooltip>
      </div>
      <v-divider></v-divider>
      <v-list-item
        v-for="(item, idx) in items"
        :key="item.name"
      >
        <v-tooltip right>
          <template v-slot:activator="{ on, attrs }">
            <v-list-item-content>
              <v-list-item-title
                class="font-weight-bold"
              >
                <span class="link"
                  v-text="(idx+1)+'.'+item.name"
                  @click="href(type, item.name)"
                ></span>
              </v-list-item-title>
              <v-progress-linear 
                striped 
                v-model="item.percent"
                v-bind="attrs"
                v-on="on"
              ></v-progress-linear>
            </v-list-item-content>
          </template>
          <div v-text="'热度:'+item.heat.toFixed(2)"></div>
        </v-tooltip>
      </v-list-item>
    </v-list>
  </v-card>
</template>

<script>
export default {
  props:{
    items: {
      type: Array,
      default:() => []
    },
    type: String
  },
  methods: {
    href(type, name) {
      if (name == null) {
        this.$notify({
          title: '数据缺失',
          message: '信息暂未收录，给您带来不便敬请谅解。',
          type: 'warning'
        });
        return;
      }
      if (type == "学科")
        type = "subject"
      else
        type = "topic"
      this.$router.push({
        path: type,
        query: { name: name }
      })
    }
  }
}
</script>

<style>
.link {
  color: #000000;
}
</style>