require "aws-sdk-core"

sqs = Aws::SQS::Client.new(region: 'us-east-1')

queue_urls = sqs.list_queues(queue_name_prefix: "widow-")[:queue_urls]

queue_urls.each do |queue|

  res = sqs.get_queue_attributes({
    queue_url: queue,
    attribute_names: ['ApproximateNumberOfMessages', 'ApproximateNumberOfMessagesNotVisible']
  })

  res = res[:attributes]

  puts "Queue: #{queue}"
  puts "    Messages:    #{res['ApproximateNumberOfMessages']}"
  puts "    Not Visible: #{res['ApproximateNumberOfMessagesNotVisible']}"
  puts

end

