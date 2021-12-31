<template>
  <v-card
    class="text-left my-2"
    max-width="1950"
  >
    <v-card-title class="d-flex">
      <v-icon class="mx-1"> mdi-text-box-multiple-outline </v-icon>
      <span>{{ patentData.title }}</span>
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
            outlined
            @click="convert()"
          >
            <v-icon>mdi-reply-outline</v-icon>
            专利转让
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
                      label="转让后的地址（不填默认不修改）"
                      required
                    ></v-text-field>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="12">
                    <v-text-field
                      v-model="submitItem.agency"
                      label="转让后的代理机构（不填默认不修改）"
                    ></v-text-field>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="12">
                    <v-text-field
                      v-model="submitItem.agent"
                      label="转让后的代理人（不填默认不修改）"
                    ></v-text-field>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="12">
                    <v-text-field
                      v-model="submitItem.applicant"
                      label="转让后的申请人（不填默认不修改）"
                    ></v-text-field>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="12">
                    <v-text-field
                      v-model="submitItem.transferee"
                      label="受让方"
                      :rules="inputRules"
                    ></v-text-field>
                  </v-col>
                </v-row>
                <v-row>
                  <v-col cols="12">
                    <v-text-field
                      v-model="submitItem.transferor"
                      label="转让方"
                      :rules="inputRules"
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
                  <v-col cols="9">
                    <v-file-input
                      class="file"
                      chips
                      label="上传证明文件"
                      @change="selectFile"
                    ></v-file-input>
                  </v-col>
                  <v-col>
                    <v-btn @click="upload()">上传</v-btn>
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
    <v-card-subtitle
      v-if="patentData.address"
      class="pb-0"
    >
      <span>
        地址:<b>{{ patentData.address }}&nbsp;</b>
      </span>
    </v-card-subtitle>
    <v-card-subtitle
      v-if="patentData.patentNum"
      class="pb-0"
    >
      <span>
        申请(专利)号:<b>{{ patentData.patentNum }}&nbsp;</b>
      </span>
      <span>
        申请日:<b>{{ patentData.fillingDate }}</b>
      </span>
    </v-card-subtitle>
    <v-card-subtitle class="py-0">
      <span v-if="patentData.authorizationDate">
        授权公告日:<b>{{ patentData.authorizationDate }}&nbsp;</b>
      </span>
      <span v-if="patentData.authorizationNum">
        授权公布号:<b>{{ patentData.authorizationNum }}</b>
      </span>
    </v-card-subtitle>
    <v-card-subtitle class="pb-0">
      <span v-if="patentData.publicationDate">
        公开公告日:<b>{{ patentData.publicationDate }}&nbsp;</b>
      </span>
      <span v-if="patentData.publicationNum">
        申请公布号:<b>{{ patentData.publicationNum }}</b>
      </span>
    </v-card-subtitle>
    <v-card-text
      v-if="patentData.inventors.length != 0"
      class="pb-0"
    >
      <span>发明人:&nbsp;</span>
      <span
        v-for="(inventor, idx) in patentData.inventors"
        :key="inventor.id"
      >
        <b
          v-if="patentData.inventors && idx == patentData.inventors.length - 1"
          v-text="inventor.name"
        ></b>
        <b
          v-else
          v-text="inventor.name + '; '"
        ></b>
      </span>
    </v-card-text>
    <v-card-text
      v-if="patentData.agent"
      class="py-0"
    >
      <span>代理人:&nbsp;</span>
      <b>{{ patentData.agent }}&nbsp;&nbsp;</b>
      <span v-if="patentData.agency">代理机构:&nbsp;<b>{{ patentData.agency }}</b></span>
    </v-card-text>
    <v-card-text
      v-if="patentData.applicant"
      class="py-0"
    >
      <span>申请人:&nbsp;</span>
      <b>{{ patentData.applicant }}</b>
    </v-card-text>
    <v-card-text v-if="patentData.abstract">
      <b> 摘要: </b>
      <span
        v-if="expand2"
        v-text="patentData.abstract"
      ></span>
      <span
        v-else
        v-text="$options.filters.abstract(patentData.abstract)"
      ></span>
      <v-btn
        x-small
        outlined
        v-if="patentData.abstract && patentData.abstract > 600"
        @click="expand2 = !expand2"
      >
        <span v-if="expand2">收起</span>
        <span v-else>全部</span>

        <v-icon
          small
          v-if="expand2"
        >mdi-chevron-up</v-icon>
        <v-icon
          small
          v-else
        >mdi-chevron-down</v-icon>
      </v-btn>
    </v-card-text>
    <v-card-text v-if="patentData.claim">
      <b> 主权项: </b>
      <span
        v-if="expand1"
        v-text="patentData.claim"
      ></span>
      <span
        v-else
        v-text="$options.filters.abstract(patentData.claim)"
      ></span>
      <v-btn
        x-small
        outlined
        v-if="patentData.claim && patentData.claim.length > 600"
        @click="expand1 = !expand1"
      >
        <span v-if="expand1">收起</span>
        <span v-else>全部</span>

        <v-icon
          small
          v-if="expand1"
        >mdi-chevron-up</v-icon>
        <v-icon
          small
          v-else
        >mdi-chevron-down</v-icon>
      </v-btn>
    </v-card-text>
    <v-card-text
      v-if="patentData.countryProvinceCode"
      class="py-0"
    >
      <span>国省代码:</span>&nbsp;
      <b>{{ patentData.countryProvinceCode }}</b>
    </v-card-text>
    <v-card-text
      v-if="patentData.pageNum"
      class="py-0"
    >
      <span>页数:</span>&nbsp;
      <b>{{ patentData.pageNum }}</b>
    </v-card-text>
    <v-card-text
      v-if="patentData.type"
      class="pt-0"
    >
      <span>专利类型:</span>&nbsp;
      <b>{{ patentData.type }}</b>
    </v-card-text>
    <v-card-text
      v-if="patentData.mainClassificationNum"
      class="py-0"
    >
      <span>主分类号:</span>&nbsp;
      <b>{{ patentData.mainClassificationNum }}</b>
    </v-card-text>
    <v-card-text
      v-if="patentData.classificationNum"
      class="py-0"
    >
      <span>分类号:</span>&nbsp;
      <b>{{ patentData.classificationNum }}</b>
    </v-card-text>
  </v-card>
</template>

<script>
export default {
  props: {
    patentData: {
      type: Object,
      default: () => {},
    },
  },
  data() {
    return {
      inputRules: [(v) => !!v || "该项未填写"],
      expand1: false,
      expand2: false,
      timer: "",
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
      email: "",
      results: {},
      page: 0,
      hasMore: false,
      dialog: false,
      thisItem: {},
      currentFile: {},
    };
  },
  filters: {
    abstract(text) {
      if (!text) return " ";
      if (text.length > 600) {
        return text.slice(0, 600) + "...";
      }
      return text;
    },
  },
  methods: {
    upload() {
      console.log(this.currentFile);
      let formData = new window.FormData();
      formData.append("file", this.currentFile);
      this.$axios({
        method: "post",
        url: "/api/resource/upload",
        data: formData,
      })
        .then((response) => {
          console.log(response.data);
          if (response.data.success) {
            this.fileToken = response.data.data;
            this.$notify({
              title: "upload",
              message: "上传成功",
              type: "success",
            });
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    submit() {
      if (
        this.submitItem.transferee === "" ||
        this.submitItem.transferor === ""
      ) {
        this.$notify({
          title: "错误",
          message: "表单未填写完成",
          type: "error",
        });
      } else if (this.websiteLink === "" && this.fileToken === "") {
        this.$notify({
          title: "错误",
          message: "网址证明或文件证明至少需提交一份",
          type: "error",
        });
      } else {
        if (this.submitItem.address === "") {
          this.submitItem.address = this.patentData.address;
        }
        if (this.submitItem.agency === "") {
          this.submitItem.agency = this.patentData.agency;
        }
        if (this.submitItem.agent === "") {
          this.submitItem.agent = this.patentData.agent;
        }
        if (this.submitItem.applicant === "") {
          this.submitItem.applicant = this.patentData.applicant;
        }
        if (this.fileToken === "") {
          this.fileToken = null;
        }
        if (this.websiteLink === "") {
          this.websiteLink = null;
        }
        if (this.email === "") {
          this.email === null;
        }
        console.log(this.submitItem.applicant);
        console.log(this.submitItem.agency);
        console.log(this.submitItem.agent);
        console.log(this.submitItem.address);
        console.log(this.patentData.id);
        this.$axios({
          method: "post",
          url: "/api/account/patent/transfer",
          data: {
            content: {
              address: this.submitItem.address,
              agency: this.submitItem.agency,
              agent: this.submitItem.agent,
              applicant: this.submitItem.applicant,
              patentId: this.patentData.id,
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
          this.submitItem.agency = "";
          this.submitItem.agent = "";
          this.submitItem.address = "";
          this.submitItem.applicant = "";
          this.submitItem.transferee = "";
          this.submitItem.transferor = "";
          this.currentFile = null;
          this.fileToken = "";
          this.websiteLink = "";
          this.email = "";
        });
      }
    },
    selectFile(file) {
      this.currentFile = file;
    },
    convert(item) {
      this.thisItem = Object({}, item);
      this.dialog = true;
    },
    href(type, id) {
      if (id == null) {
        this.$notify({
          title: "数据缺失",
          message: "信息暂未收录，给您带来不便敬请谅解。",
          type: "warning",
        });
        return;
      }
      this.$router.push({
        path: type,
        query: { id: id },
      });
    },
    hrefname(type, name) {
      if (name == null) {
        this.$notify({
          title: "数据缺失",
          message: "信息暂未收录，给您带来不便敬请谅解。",
          type: "warning",
        });
        return;
      }
      this.$router.push({
        path: type,
        query: { name: name },
      });
    },
    toSource(url) {
      window.location.href = url;
    },
  },
  mounted() {
    this.timer = setTimeout(() => {
      console.log(this.patentData);
    }, 2000);
  },
};
</script>

<style>
</style>
