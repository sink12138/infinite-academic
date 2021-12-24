<template>
  <div>
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
          @click="convert()"
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
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="submitItem.agent"
                    label="转让后的代理人（不填默认不修改）"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="submitItem.applicant"
                    label="转让后的申请人（不填默认不修改）"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="submitItem.transferee"
                    label="受让方"
                    required
                  ></v-text-field>
                </v-col>
              </v-row>
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="submitItem.transferor"
                    label="转让方"
                    required
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
      email:"",
      results: {},
      page: 0,
      hasMore: false,
      dialog: false,
      thisItem: {},
      currentFile: {},
    };
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
    upload() {
      let formData = new window.FormData();
      formData.append("file", this.currentFile);
      if (this.fileToken != null) {
        formData.append("token", this.fileToken);
      }
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
      let formData = new window.FormData();
      formData.append("file", this.currentFile);
      if (this.fileToken != null) {
        formData.append("token", this.fileToken);
      }
      this.$axios({
        method: "post",
        url: "/api/resource/upload",
        data: formData,
      }).then((response) => {
        console.log(response.data);
        if (response.data.success) {
          this.fileToken = response.data.data;
          this.$notify({
            title: "upload",
            message: "上传成功",
            type: "success",
          });
        }
      });
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
    selectFile(file) {
      this.currentFile = file;
    },
    convert() {
      this.dialog = true;
    },
  },
};
</script>
