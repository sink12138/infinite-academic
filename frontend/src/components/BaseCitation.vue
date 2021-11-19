<template>
  <v-menu
    :close-on-content-click="false"
    :nudge-width="320"
    rounded="sm"
    offset-y
  >
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        v-bind="attrs"
        v-on="on"
        depressed
        height="100%"
      >
        引用
        <v-icon>mdi-comma</v-icon>
      </v-btn>
    </template>

    <v-card
      rounded="sm"
    >
      <v-card-title
        class="text-h5"
      >
        引用列表
      </v-card-title>
      <v-card-text>
        <v-list 
          max-height="300"
          class="overflow-auto"
        >
          <v-list-item
            class="grey lighten-4 my-1"
            v-for="citation in citationList"
            :key="citation.paperId"
          >
            <v-list-item-content v-text="citation.text">
            </v-list-item-content>
            <v-list-item-action>
              <v-btn icon>
                <v-icon 
                  color="red darken-2"
                  @click="deleteItem(citation)"
                >
                  mdi-window-close
                </v-icon>
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </v-list>
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          depressed
          @click="reveal = true"
        >删除所有
        </v-btn>
        <v-btn 
          depressed
          @click="copyCitations()"
        >复制
        </v-btn>
      </v-card-actions>

      <v-expand-transition>
        <v-card
          v-if="reveal"
          class="transition-fast-in-fast-out v-card--reveal 
          d-flex flex-column justify-center align-center"
          style="height: 100%"
          dark
        >
          <div>
            <v-card-text
              class="text-center text-h6"
            >
              是否确认删除所有引用？
            </v-card-text>
            <v-divider></v-divider>
            <v-card-actions
              class="text-center"
            >
              <v-btn
                color="grey"
                class="font-weight-bold"
                plain
                x-large
                @click="reveal = false"
              >
                取消
              </v-btn>
              <v-btn
                color="error"
                class="font-weight-bold"
                plain
                x-large
                @click="cleanCitaions()"
              >
                删除
              </v-btn>
            </v-card-actions>
          </div>
        </v-card>
      </v-expand-transition>
    </v-card>
  </v-menu>
</template>

<script>
export default {
  data:() =>  ({
    expand: false,
    reveal: false,
    citationList: [],
  }),

  created() {
    this.initCitations();
  },

  methods: {
    initCitations() {
      this.citationList = localStorage.getItem("citations");
    },
    copyCitations() {
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
    deleteItem(citation) {
      var index = this.citationList.indexOf(citation);
      this.citationList.splice(index, 1);
      localStorage.setItem("citations", this.citationList)
    },
    cleanCitaions() {
      this.citationList.splice(0,this.citationList.length);
      this.reveal = false;
      localStorage.removeItem("citations");
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