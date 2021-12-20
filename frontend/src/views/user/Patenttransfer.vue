<template>
  <div>
    <div
      class="whole"
      style="width:60%"
    >
      <v-container>
        <v-card
          calss="rounded-lg"
          elevation=6
          width=100%
        >
          <v-card-title>专利转移申请</v-card-title>
          <div class="whole">
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="address"
                  label="转让后的地址"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="agency"
                  label="转让后的代理机构"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="agent"
                  label="转让后的代理人"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="applicant"
                  label="转让后的申请人"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="patentId"
                  label="专利的数据库id"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="transferee"
                  label="受让方"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="transferor"
                  label="转让方"
                  required
                ></v-text-field>
              </v-col>
            </v-row>
          </div>
          <v-btn
            color="primary"
            @click="submit()"
          >
            提交申请
          </v-btn>
        </v-card>
      </v-container>
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {
      valid: false,
      address: "",
      agency: "",
      agent: "",
      applicant: "",
      patentId: "",
      transferee: "",
      transferor: "",
      email: "",
      fileToken: "",
      websiteLink: "",
    };
  },
  methods: {
    submit() {
      this.$axios({
        method: "post",
        url: "/api/account/patent/transfer",
        data: {
          content: {
            address: this.address,
            agency: this.agency,
            agent: this.agent,
            applicant: this.applicant,
            patentId: this.patentId,
            transferee: this.transferee,
            transferor: this.transferor,
          },
        },
      }).then((response) => {
        console.log(response.data);
        if (response.data.success) {
          this.$notify({
            title: "成功",
            message: "发送申请成功",
            type: "success",
          });
        } else {
          this.$notify({
            title: "失败",
            message: "发送申请失败",
            type: "error",
          });
        }
      });
    },
  },
};
</script>

<style>
</style>
