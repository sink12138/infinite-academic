<template>
  <div>
    <div
      v-for="item in results"
      :key="item.id"
    >
      <!-- 专利 -->
      <v-card
        class="text-left my-2"
        max-width="650"
      >
        <v-card-title class="d-flex">
          <v-icon class="mx-1">
            mdi-text-box-multiple-outline
          </v-icon>
          <span
            class="link"
            @click="href('patent', item.id)"
            v-html="item.title"
          ></span>
          <v-spacer></v-spacer>
          <v-dialog
            v-model="dialog"
            persistent
            max-width="600px"
          >
            <template v-slot:activator="{ on, attrs }">
              <v-btn
                depressed
                height="100%"
                v-bind="attrs"
                v-on="on"
                icon
                @click="convert(item)"
              >
                <v-icon>mdi-delete</v-icon>
              </v-btn>
            </template>
            <v-card>
              <v-card-title>
                <span class="headline">专利转移</span>
              </v-card-title>
              <v-card-text>
                <v-container>
                  <div class="whole">
                    <v-row>
                      <v-col cols="12">
                        <v-text-field
                          v-model="submitItem.address"
                          label="转让后的地址"
                          required
                        ></v-text-field>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col cols="12">
                        <v-text-field
                          v-model="submitItem.agency"
                          label="转让后的代理机构"
                          required
                        ></v-text-field>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col cols="12">
                        <v-text-field
                          v-model="submitItem.agent"
                          label="转让后的代理人"
                          required
                        ></v-text-field>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col cols="12">
                        <v-text-field
                          v-model="submitItem.applicant"
                          label="转让后的申请人"
                          required
                        ></v-text-field>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col cols="12">
                        <v-text-field
                          v-model="submitItem.transferee"
                          label="受让方（可选填）"
                        ></v-text-field>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col cols="12">
                        <v-text-field
                          v-model="submitItem.transferor"
                          label="转让方(可选填）"
                        ></v-text-field>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col cols="12">
                        <v-text-field
                          v-model="email"
                          label="邮箱（可选填）"
                        ></v-text-field>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col cols="12">
                        <v-text-field
                          v-model="websiteLink"
                          label="相关网站链接（可选填）"
                        ></v-text-field>
                      </v-col>
                    </v-row>
                    <v-row>
                      <v-col cols="12">
                        <v-file-input
                          chips
                          label="上传证明文件"
                        ></v-file-input>
                      </v-col>
                    </v-row>
                  </div>
                  <v-btn
                    color="primary"
                    large
                    @click="dialog = false"
                    width="160px"
                  >
                    返回
                  </v-btn>
                  &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;
                  <v-btn
                    color="primary"
                    large
                    v
                    @click="submit()"
                    width="160px"
                  >
                    提交申请
                  </v-btn>
                </v-container>
              </v-card-text>
            </v-card>
          </v-dialog>
        </v-card-title>
        <v-card-subtitle class="pb-0">
          <span v-if="item.fillingDate">
            申请日:{{ item.fillingDate }} </span>&nbsp;
          <span v-if="item.publicationDate">
            公开日:{{ item.publicationDate }} </span>&nbsp;
          <span v-if="item.applicant">申请人:{{ item.applicant }}</span>&nbsp;
        </v-card-subtitle>
        <v-card-text class="pb-0">
          <span
            v-for="(inventor, idx) in item.inventors"
            :key="inventor.id"
          >
            <span
              class="link"
              v-if="idx == item.inventors.length - 1"
              @click="href('author', inventor.id)"
            >
              {{ inventor.name }}
            </span>
            <span
              class="link"
              v-else
              @click="href('author', inventor.id)"
            >
              {{ inventor.name + "," }}
            </span>
          </span>
        </v-card-text>
      </v-card>
    </div>
    <div>
      <v-btn
        color="primary"
        v-if="page!=0"
        @click="prePage()"
      >上一页</v-btn>
      <v-spacer></v-spacer>
      <v-btn
        color="primary"
        v-if="hasMore"
        @click="nextPage()"
      >下一页</v-btn>
    </div>
  </div>
</template>

<script>
export default {
  components: {},
  data() {
    return {
      submitItem: {
        address: "",
        agency: "",
        agent: "",
        applicant: "",
        transferee: "",
        transferor: "",
      },
      fileToken: "",
      websiteLink: "",
      patentId: "",
      results: {},
      page: 0,
      hasMore: false,
      dialog: false,
      thisItem: {},
    };
  },
  mounted() {
    /*this.$axios({
      method: "get",
      url: "/api/account/profile",
    }).then((response) => {
      console.log(response.data);
      if (response.data.success === true) {
        var i = 0;
        for (var p in response.data.data) {
          if (p === "researcherId") {
            this.id = response.data.data.researcherId;
          }
        }
        console.log(this.id);
        console.log(i);
        if (this.id === "abc") {
          this.$notify({
            title: "提示",
            message: "您还不是学者，无需转移专利",
            type: "warning",
          });
        }
      }
    });*/
    this.getPatents();
    this.$axios({
      method: "get",
      url: "/api/search/info/patent/" + this.thisItem.id,
    }).then((response) => {
      console.log(response.data);
      if (response.data.success) {
        this.submitItem.address = response.data.data.address;
        this.submitItem.agency = response.data.data.agency;
        this.submitItem.agent = response.data.data.agent;
        this.submitItem.applicant = response.data.data.applicant;
      }
    });
    this.patentId = this.thisItem.id;
  },
  methods: {
    getPatents() {
      this.$axios({
        method: "get",
        url: "/api/search/relation/inventions/" + this.id + "/" + this.page,
      }).then((response) => {
        console.log(response.data);
        if (!response.data.success) {
          console.log(response.data.message);
        } else if (response.data.data.items === undefined) {
          this.$notify({
            title: "提示",
            message: "您暂无专利成果",
            type: "warning",
          });
        } else {
          this.results = response.data.data.items;
          this.hasMore = response.data.data.hasMore;
        }
      });
    },
    prePage() {
      this.page = this.page - 1;
      this.getPatents();
    },
    nextPage() {
      this.page = this.page + 1;
      this.getPatents();
    },
    submit() {
      this.$axios({
        method: "post",
        url: "/api/account/patent/transfer",
        data: {
          content: {
            address: this.submitItem.address,
            agency: this.submitItem.agency,
            agent: this.submitItem.agent,
            applicant: this.submitItem.applicant,
            patentId: this.patentId,
            transferee: this.submitItem.transferee,
            transferor: this.submitItem.transferor,
          },
          email: this.email,
          fileToken: this.fileToken,
          websiteLink: this.websiteLink,
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success) {
          this.$notify({
            title: "成功",
            message: "发送申请成功，请等待工作人员审核",
            type: "success",
          });
          this.dialog = false;
        } else {
          this.$notify({
            title: "失败",
            message: "发送申请失败，请检查表单信息",
            type: "error",
          });
        }
      });
    },
    convert(item) {
      this.thisItem = Object({}, item);
      this.dialog = true;
    },
  },
};
</script>
