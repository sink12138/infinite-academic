<template>
  <v-card>
    <v-card-title>
      引用列表
    </v-card-title>
    <v-card-text>
      <v-list>
        <v-list-item
          v-for="citation in citationList"
          :key="citation.paperId"
        >
          <v-list-item-content v-text="citation.text">
          </v-list-item-content>
          <v-list-item-action>
            <v-btn icon>
              <v-icon color="red darken-2">mdi-window-close</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-card-text>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn
        @click="reveal = true"
      >删除所有
      </v-btn>
      <v-btn 
        @click="citationCopy"
      >复制
      </v-btn>
    </v-card-actions>

    <v-expand-transition>
      <v-card
        v-show="reveal"
        class="transition-fast-in-fast-out v-card--reveal"
        style="height: 100%;"
      >
        <v-card-text>
          确认清除？
        </v-card-text>
        <v-card-actions>
          <v-btn
            text
            color="teal accent-4"
            @click="reveal = false"
          >
            取消
          </v-btn>
          <v-btn
            text
            color="teal accent-4"
            @click="reveal = false"
          >
            删除
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-expand-transition>
  </v-card>
</template>

<script>
export default {
  data:() =>  ({
    expand: false,
    citationList: [
      {paperId:1, text: 'hello'},
      {paperId:2, text: 'hello'}
    ],
    reveal: false
  }),
  methods: {
    citationCopy() {
      this.$copyText(this.citationList)
      .then(e => {
        this.$notify({
          title: 'Copy',
          message: e.text,
          type: 'success'
        });
      }).catch(error => {
        console.log(error)
      })
    },
    citationClean() {
      this.cleanExpand = true;
      //this.citationList.splice(0,this.citationList.length);
    },
    onClickOutside() {
      this.expand = false;
    }
  }
}
</script>

<style lang="scss">
.v-card--reveal {
  bottom: 0;
  opacity: 1 !important;
  position: absolute !important;
  width: 100%;
}
</style>