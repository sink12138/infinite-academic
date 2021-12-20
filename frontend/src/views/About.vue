<template>
  <message-dialog :message="test"></message-dialog>
</template>

<script>
import MessageDialog from "../components/MessageDialog.vue";

export default {
  components: {
    MessageDialog
  },
  data: () => ({
    test: 
    {
      basic: {
        "email": "114514@qq.com",
        "fileToken": "B0KAR4A8Q3BHHHPMF0TT0I0B6YQMBAPPPHNPMIYM6GJMDZF4EQB7CLGOCP8S211R",
        "id": "BcYuon0BGFAD_tUkFP7g",
        "status": "审核中",
        "time": "2021-08-17 19:26",
        "type": "专利转让",
        "userId": "3cYPmX0BGFAD_tUkT_03",
        "websiteLink": ""
      },
      content: {}
    }
  }),
  methods: {
    
  },
};
</script>


<style>
</style>
