<template>
  <v-menu
    :close-on-content-click="false"
    nudge-width="450"
    max-width="450"
    rounded="sm"
    offset-y
  >
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        v-bind="attrs"
        v-on="on"
        depressed
      >
        引用
        <v-icon>mdi-comma</v-icon>
      </v-btn>
    </template>

    <v-card
      max-width="450"
      rounded="sm"
    >
      <v-card-title
        class="text-center text-h4"
      >
        引用列表
        <v-spacer></v-spacer>
        <v-icon large>mdi-comma-box</v-icon>
        <v-icon large>mdi-comma-box</v-icon>
      </v-card-title>


      <v-card-text
        v-if="citationList.length > 0"
      >
        <v-list 
          max-height="300"
          max-width="468"
          class="overflow-auto"
        >
          <v-list-item
            class="grey lighten-4 my-1"
            v-for="citation in citationList"
            :key="citation.paperId"
          >
            <div v-html="citation[citationType]">
            </div>
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

      <v-card-text
        v-else
        class="text-left text-body-1"
      >
        <v-row>
          <v-col 
            align-self="center"
            cols="2"
          >
            <v-icon x-large full-height>mdi-information</v-icon>
          </v-col>
          <v-col
            align-self="center"
          >
            <div class="text-h6">尚未选择任何论文引用</div>
          </v-col>
        </v-row>
        <br/>
        <div>点击论文旁的<b>“引用”</b>按钮，将其添加到引用列表</div>
        <div>添加引用信息后可以修改引用规范并一键复制</div>
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions
        v-if="citationList.length > 0"
      >
        <v-btn-toggle
          v-model="citationType"
          group
          color="indigo darken-4"
        >
          <v-btn
            value="MLA"
          >MLA</v-btn>
          <v-btn
            value="APA"
          >APA</v-btn>
          <v-btn
            value="GBT"
          >GB/T</v-btn>
        </v-btn-toggle>
        <v-spacer></v-spacer>
        <v-btn
          color="deep-orange accent-4"
          plain
          @click="reveal = true"
        >
          <v-icon>mdi-trash-can</v-icon>
          清空
        </v-btn>
        <v-btn 
          plain
          @click="copyCitations()"
        >
          <v-icon>mdi-clipboard-text</v-icon>
          复制
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
    citationType: "MLA",
    citationList: [],
  }),

  created() {
    this.loadCitations();
  },

  watch: {
    '$store.state.citations'() {
      this.loadCitations();
    }
  },

  methods: {
    loadCitations() {
      this.citationList = [];
      var citations = JSON.parse(localStorage.getItem("citations"));
      for (var i in citations) {
        this.citationList.push(citations[i]);
      }
    },
    copyCitations() {
      var citationText = "";
      for (var i in this.citationList){
        citationText += this.citationList[i]["MLA"] + "\n";
      }
      this.$copyText(citationText)
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
      var citations = JSON.parse(localStorage.getItem("citations"));
      var index = this.citationList.indexOf(citation);
      delete citations[citation.paperId];
      this.citationList.splice(index, 1);
      if (JSON.stringify(citations) == "{}")
        localStorage.removeItem("citations");
      else
        localStorage.setItem("citations", JSON.stringify(citations));
      this.$store.commit('decCitations');
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