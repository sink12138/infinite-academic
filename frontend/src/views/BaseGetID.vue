<template>
  <v-card
    calss="rounded-lg"
    elevation=6
    width=100%
  >
    <div class="whole">
      <v-row>
        <v-col>
          <v-select
            :items="types"
            label="查询类型"
            solo
          ></v-select>
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <v-text-field
            v-model="message"
            label="条件"
            required
          ></v-text-field>
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <v-btn @click="get()">
            查询
          </v-btn>
        </v-col>
        <v-col>
          <v-btn @click="close()">
            关闭
          </v-btn>
        </v-col>
      </v-row>
      <br/>
    </div>
    
  </v-card>
</template>
<script>
  export default {
    data(){
      return{
        types:["文献","作者","机构","期刊"],
        
      }
    },
    methods:{
      get(){

      },
      close(){
        this.$emit('closeID',"close")
      }
    }
  }
</script>